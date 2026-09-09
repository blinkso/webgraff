# Webchat flow support in 1.0.14

The attendee webchat application needs to restore a handler after a restart and render actions without interpreting human-readable message text.

`HandlersFilter` exposes current-state, restore and resume operations, plus optional checkpoint and validation callbacks. A transport can retain the current step on unexpected failures and retry final submission explicitly. Resuming a conversation never runs finalization by itself.

`SendRequest.metadata` carries application-owned presentation data. It is excluded from ordinary JSON serialization and consumed by the webchat transport. Validation replies retain the current question's metadata. Message formatting preserves structured button attributes instead of inventing actions from numbered text.

Explicit webchat inputs bypass legacy text-to-button matching. Legacy matching requires a nonempty exact label. Command matching checks command boundaries, so ordinary attendee answers cannot accidentally start an unrelated handler.

Transport error logs omit message bodies. Each module's JAR has its own filename so the bot distribution can contain all framework modules.

## Release and validation

Version 1.0.14 is consumed by the companion `blink-web-bot` PR. The existing publish workflow runs on the default branch; PR creation does not publish the library.

The framework compiled as part of the bot's clean composite build; all 79 bot tests and `installDist` passed. These include handler restoration, validation, retries, structured actions and concurrent inputs. Local checks used JDK 18 with Java 17 bytecode targets. Package metadata is generated separately before publication.

Publish 1.0.14 before deploying the bot. For local development, the bot can opt into this checkout with `-PwebgraffSource=/path/to/webgraff`.
