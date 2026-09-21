/**
 * Outbox application behaviour: enqueueing events, reading and rescheduling stored rows, and
 * publishing confirmed events to RabbitMQ.
 */
package wiki.chiu.micro.common.outbox.application;
