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

package fluence.ethclient

import java.io.File

import cats.effect.{ContextShift, IO, Timer}
import fluence.ethclient.MasterNodeApp.getClass
import fluence.ethclient.data.{DeployerContractConfig, SolverInfo}
import fluence.ethclient.helpers.RemoteCallOps._
import fluence.ethclient.helpers.Web3jConverters._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint8
import slogging.MessageFormatter.DefaultPrefixFormatter
import slogging.{LazyLogging, LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.sys.process.{Process, ProcessLogger, _}
import scala.util.Random

/**
 * Integration test for Deployer contract and Master node
 */
class ContractMasterIntegrationSpec extends FlatSpec with LazyLogging with Matchers with BeforeAndAfterAll {

  implicit private val ioTimer: Timer[IO] = IO.timer(global)
  implicit private val ioShift: ContextShift[IO] = IO.contextShift(global)

  private val url = sys.props.get("ethereum.url")
  private val client = EthClient.makeHttpResource[IO](url)

  val dir = new File("../bootstrap")
  def run(cmd: String): Unit = Process(cmd, dir).!(ProcessLogger(_ => ()))
  def runBackground(cmd: String): Unit = Process(cmd, dir).run(ProcessLogger(_ => ()))

  override protected def beforeAll(): Unit = {
    logger.info("bootstrapping npm")
    run("npm install")

    logger.info("starting Ganache")
    runBackground("npm run ganache")

    logger.info("deploying Deployer.sol Ganache")
    run("npm run migrate")
  }

  override protected def afterAll(): Unit = {
    logger.info("killing ganache")
    run("pkill -f ganache")
  }

  "Ethereum client" should "receive an event" in {
    val str = Random.alphanumeric.take(10).mkString
    val bytes = stringToBytes32(str)
    val contractAddress = "0x9995882876ae612bfd829498ccd73dd962ec950a"
    val owner = "0x4180FC65D613bA7E1a385181a219F1DBfE7Bf11d"

    client.use { ethClient =>
      for {
        contract <- ethClient.getDeployer[IO](contractAddress, owner)

        whitelistTxReceipt <- contract.addAddressToWhitelist(new Address(owner)).call[IO]
        _ = assert(whitelistTxReceipt.isStatusOK)

        codeTxReceipt <- contract.addCode(bytes, bytes, new Uint8(1)).call[IO]
        _ = assert(codeTxReceipt.isStatusOK)
      } yield ()
    }.unsafeRunSync()

    val longTermKeysDir = getClass.getClassLoader.getResource("long-term-keys/node0").getPath
    val solverInfo =
      SolverInfo(List(longTermKeysDir, "192.168.0.5", "25000", "25001")).value
        .unsafeRunSync()
        .left
        .map(x => throw x)
        .getOrElse(???)

    PrintLoggerFactory.formatter = new DefaultPrefixFormatter(false, false, false)
    LoggerConfig.factory = PrintLoggerFactory()
    LoggerConfig.level = LogLevel.INFO
    MasterNodeApp.runMasterNode(solverInfo, DeployerContractConfig(owner, contractAddress)).unsafeRunSync()
  }
}
