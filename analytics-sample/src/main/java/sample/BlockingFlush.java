package sample;

import com.hightouch.analytics.Analytics;
import com.hightouch.analytics.Callback;
import com.hightouch.analytics.MessageTransformer;
import com.hightouch.analytics.Plugin;
import com.hightouch.analytics.messages.Message;
import com.hightouch.analytics.messages.MessageBuilder;
import java.util.concurrent.Phaser;

/**
 * The {@link Analytics} class doesn't come with a blocking {@link Analytics#flush()} implementation
 * out of the box. It's trivial to build one using a {@link Phaser} that monitors requests and is
 * able to block until they're uploaded.
 *
 * <pre><code>
 * BlockingFlush blockingFlush = BlockingFlush.create();
 * Analytics analytics = Analytics.builder(writeKey)
 *      .plugin(blockingFlush.plugin())
 *      .build();
 *
 * // Do some work.
 *
 * analytics.flush(); // Trigger a flush.
 * blockingFlush.block(); // Block until the flush completes.
 * analytics.shutdown(); // Shut down after the flush is complete.
 * </code></pre>
 */
public class BlockingFlush {

  public static BlockingFlush create() {
    return new BlockingFlush();
  }

  BlockingFlush() {
    this.phaser = new Phaser(1);
  }

  final Phaser phaser;

  public Plugin plugin() {
    return new Plugin() {
      @Override
      public void configure(Analytics.Builder builder) {
        builder.messageTransformer(
            new MessageTransformer() {
              @Override
              public boolean transform(MessageBuilder builder) {
                phaser.register();
                return true;
              }
            });

        builder.callback(
            new Callback() {
              @Override
              public void success(Message message) {
                phaser.arrive();
              }

              @Override
              public void failure(Message message, Throwable throwable) {
                phaser.arrive();
              }
            });
      }
    };
  }

  public void block() {
    phaser.arriveAndAwaitAdvance();
  }
}
