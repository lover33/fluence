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

package fluence.statemachine.state

import cats.Monad
import cats.data.{EitherT, StateT}
import cats.effect.concurrent.MVar
import cats.syntax.flatMap._
import cats.syntax.functor._
import fluence.statemachine.tree.{MerkleTreeNode, TreeNode}
import fluence.statemachine.util.ClientInfoMessages

import scala.language.higherKinds

/**
 * Holds [[TendermintState]], providing their underlying states to another components for accessing and modifying.
 *
 * `Height`-th state means the state which `app hash` is contained in `Height`-th block, so this is the state upon
 * `Commit` of `Height-1`-th block.
 * See [[https://tendermint.readthedocs.io/projects/tools/en/master/abci-spec.html spec.]]
 * So, if `H`-th state (state with height=`H`) requested, then this is state obtained on `H-1`-th `Commit` processing
 * (1-indexed) – this is `H-2`-th item of `storage` list (0-indexed).
 *
 * @param tendermintState current set of states (Consensus, Mempool and Query) used to process Tendermint requests
 */
class TendermintStateHolder[F[_]: Monad](private val tendermintState: MVar[F, TendermintState]) {

  /**
   * Returns `height` corresponding to the latest committed and, at the same time, the latest verifiable state.
   */
  def latestCommittedHeight: F[Long] = tendermintState.read.map(x => x.latestCommittedHeight)

  /**
   * Returns `Query` state.
   *
   * @return either height and corresponding state or error message
   */
  def queryState: EitherT[F, String, (Long, MerkleTreeNode)] =
    EitherT.fromOptionF(
      tendermintState.read.map(x => x.queryState.map(y => (x.latestCommittedHeight, y))),
      ClientInfoMessages.QueryStateIsNotReadyYet
    )

  /**
   * Returns state used for `CheckTx` method transaction validation.
   */
  def mempoolState: F[MerkleTreeNode] =
    tendermintState.read.map(x => x.mempoolState.getOrElse(TreeNode.emptyMerkelizedNode))

  /**
   * Returns current Consensus state.
   */
  def consensusState: F[TreeNode] = tendermintState.read.map(_.consensusState)

  /**
   * Modifies current Consensus state.
   *
   * @param modifier modifying [[cats.data.StateT]] to change Consensus state
   * @tparam V type of returned value
   */
  def modifyConsensusState[V](modifier: StateT[F, TreeNode, V]): F[V] =
    modifyStates(TendermintState.modifyConsensusState(modifier))

  /**
   * Modifies states.
   *
   * @param modifier modifying [[cats.data.StateT]] to change states
   * @tparam V type of returned value
   */
  def modifyStates[V](modifier: StateT[F, TendermintState, V]): F[V] =
    for {
      oldStates <- tendermintState.take
      runResult <- modifier.run(oldStates)
      (newStates, v) = runResult
      _ <- tendermintState.put(newStates)
    } yield v
}
