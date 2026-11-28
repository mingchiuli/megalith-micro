//! Request-triggered counters and read updates must finish before service shutdown.
use std::{future::Future, time::Duration};
use tokio_util::task::TaskTracker;
use tracing::{Instrument, instrument::WithSubscriber};

#[derive(Clone, Default)]
pub struct BackgroundTasks(TaskTracker);

impl BackgroundTasks {
    pub fn spawn(&self, work: impl Future<Output = ()> + Send + 'static) {
        self.0
            .spawn(work.in_current_span().with_current_subscriber());
    }

    pub async fn shutdown(&self) {
        self.0.close();
        if tokio::time::timeout(Duration::from_secs(60), self.0.wait())
            .await
            .is_err()
        {
            tracing::warn!("background tasks did not finish in 60 seconds");
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use tokio::sync::oneshot;

    #[tokio::test]
    async fn shutdown_waits_for_work_and_the_worker_observes_the_request_span() {
        let tasks = BackgroundTasks::default();
        let (release, wait) = oneshot::channel();
        let (done, observed) = oneshot::channel();
        let parent = tracing::subscriber::with_default(tracing_subscriber::registry(), || {
            let span = tracing::info_span!("source request");
            span.in_scope(|| {
                tasks.spawn(async {
                    wait.await.unwrap();
                    done.send(tracing::Span::current().id()).unwrap();
                })
            });
            span.id().unwrap()
        });
        let shutdown = tasks.shutdown();
        tokio::pin!(shutdown);
        assert!(
            tokio::time::timeout(Duration::from_millis(10), &mut shutdown)
                .await
                .is_err()
        );
        release.send(()).unwrap();
        shutdown.await;
        assert_eq!(observed.await.unwrap(), Some(parent));
    }
}
