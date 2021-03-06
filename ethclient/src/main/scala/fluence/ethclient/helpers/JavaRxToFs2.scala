/*
 * Copyright 2018 Fluence Labs Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fluence.ethclient.helpers

import cats.effect._
import cats.effect.syntax.effect._
import cats.syntax.apply._
import cats.syntax.functor._
import fs2.concurrent.Queue
import io.reactivex.Flowable

import scala.language.higherKinds

object JavaRxToFs2 {

  implicit class FlowableToStream[T](flowable: Flowable[T]) {

    /**
     * Convert the flowable to fs2.Stream, using F[_] effect.
     * No backpressure is applied; input is bufferized in a queue.
     *
     * @tparam F The effect type; it's Concurrent to enable Queue, and Effect to push to that queue from an Observer
     * @return A stream
     */
    def toFS2[F[_]: ConcurrentEffect]: fs2.Stream[F, T] = {
      fs2.Stream
        .bracket(
          Queue
            .noneTerminated[F, Either[Throwable, T]]
            .map { queue ⇒
              queue ->
                flowable.subscribe(
                  (t: T) => queue.enqueue1(Some(Right(t))).toIO.unsafeRunAsyncAndForget(),
                  (e: Throwable) => queue.enqueue1(Some(Left(e))).toIO.unsafeRunAsyncAndForget(),
                  () => queue.enqueue1(None).toIO.unsafeRunAsyncAndForget()
                )
            }
        )({
          // Release: dispose
          case (queue, disposable) ⇒
            queue.enqueue1(None) *>
              Sync[F].catchNonFatal(disposable.dispose())
        })
        .flatMap(_._1.dequeue.rethrow)

    }
  }
}
