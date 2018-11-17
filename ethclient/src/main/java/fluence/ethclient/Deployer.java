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

package fluence.ethclient;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes24;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tuples.generated.Tuple6;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import rx.Observable;
import rx.functions.Func1;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.6.0.
 */
public class Deployer extends Contract {
    private static final String BINARY = "6080604052600160075560008054600160a060020a03191633179055611c9e8061002a6000396000f3006080604052600436106100cc5763ffffffff60e060020a6000350416630988ca8c81146100d157806318b919e91461013a578063217fe6c6146101c45780632238ba2f1461023f57806324953eaa14610260578063286dd3f5146102b55780634e69d560146102d6578063715018a6146103535780637b9417c8146103685780637ea29f62146103895780638da5cb5b146103bc5780639b19251a146103ed578063c7c02e441461040e578063e2683e9214610476578063e2ec6ec314610586578063f2fde38b146105db575b600080fd5b3480156100dd57600080fd5b5060408051602060046024803582810135601f8101859004850286018501909652858552610138958335600160a060020a03169536956044949193909101919081908401838280828437509497506105fc9650505050505050565b005b34801561014657600080fd5b5061014f61066a565b6040805160208082528351818301528351919283929083019185019080838360005b83811015610189578181015183820152602001610171565b50505050905090810190601f1680156101b65780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b3480156101d057600080fd5b5060408051602060046024803582810135601f810185900485028601850190965285855261022b958335600160a060020a031695369560449491939091019190819084018382808284375094975061068f9650505050505050565b604080519115158252519081900360200190f35b34801561024b57600080fd5b5061013860043560243560ff60443516610702565b34801561026c57600080fd5b5060408051602060048035808201358381028086018501909652808552610138953695939460249493850192918291850190849080828437509497506108119650505050505050565b3480156102c157600080fd5b50610138600160a060020a036004351661085e565b3480156102e257600080fd5b506102eb6108a5565b604051808460ff1660ff16815260200183815260200180602001828103825283818151815260200191508051906020019060200280838360005b8381101561033d578181015183820152602001610325565b5050505090500194505050505060405180910390f35b34801561035f57600080fd5b5061013861094c565b34801561037457600080fd5b50610138600160a060020a03600435166109b8565b34801561039557600080fd5b5061013860043567ffffffffffffffff196024351661ffff604435811690606435166109fc565b3480156103c857600080fd5b506103d1610cd8565b60408051600160a060020a039092168252519081900360200190f35b3480156103f957600080fd5b5061022b600160a060020a0360043516610ce7565b34801561041a57600080fd5b50610426600435610d1c565b60408051602080825283518183015283519192839290830191858101910280838360005b8381101561046257818101518382015260200161044a565b505050509050019250505060405180910390f35b34801561048257600080fd5b5061048e600435610e3e565b60408051878152602080820188905291810186905260c060608201818152865191830191909152855191929091608084019160a085019160e0860191898101910280838360005b838110156104ed5781810151838201526020016104d5565b50505050905001848103835286818151815260200191508051906020019060200280838360005b8381101561052c578181015183820152602001610514565b50505050905001848103825285818151815260200191508051906020019060200280838360005b8381101561056b578181015183820152602001610553565b50505050905001995050505050505050505060405180910390f35b34801561059257600080fd5b5060408051602060048035808201358381028086018501909652808552610138953695939460249493850192918291850190849080828437509497506110eb9650505050505050565b3480156105e757600080fd5b50610138600160a060020a0360043516611138565b610666826001836040518082805190602001908083835b602083106106325780518252601f199092019160209182019101610613565b51815160209384036101000a6000190180199092169116179052920194855250604051938490030190922092915050611158565b5050565b6040805180820190915260098152600080516020611c53833981519152602082015281565b60006106fb836001846040518082805190602001908083835b602083106106c75780518252601f1990920191602091820191016106a8565b51815160209384036101000a600019018019909216911617905292019485525060405193849003019092209291505061116d565b9392505050565b61070b33610ce7565b151561071657600080fd5b604080516060810182528481526020810184815260ff8481169383019384526008805460018101825560009190915292517ff3f7a9fe364faab93b216da50a3214154f22a0a2b415b23a84c8169e8b636ee360039094029384015590517ff3f7a9fe364faab93b216da50a3214154f22a0a2b415b23a84c8169e8b636ee483015591517ff3f7a9fe364faab93b216da50a3214154f22a0a2b415b23a84c8169e8b636ee5909101805460ff1916919092161790556107d261118c565b151561080c576040805184815290517fd18fba5b22517a48b063e62f8b6acbfc4dbfba1583e929178d3fc862218544dd9181900360200190a15b505050565b60008054600160a060020a0316331461082957600080fd5b5060005b815181101561066657610856828281518110151561084757fe5b9060200190602002015161085e565b60010161082d565b600054600160a060020a0316331461087557600080fd5b6108a281604080519081016040528060098152602001600080516020611c5383398151915281525061172c565b50565b6000806060806000806008805490506040519080825280602002602001820160405280156108dd578160200160208202803883390190505b509250600091505b60085482101561093a5760088054839081106108fd57fe5b6000918252602090912060026003909202010154835160ff9091169084908490811061092557fe5b602090810290910101526001909101906108e5565b50506002546065959094509092509050565b600054600160a060020a0316331461096357600080fd5b60008054604051600160a060020a03909116917ff8df31144d9c2f0f6b59d69b8b98abd5459d07f2742c4df920b25aae33c6482091a26000805473ffffffffffffffffffffffffffffffffffffffff19169055565b600054600160a060020a031633146109cf57600080fd5b6108a281604080519081016040528060098152602001600080516020611c5383398151915281525061183d565b610a0533610ce7565b1515610a1057600080fd5b60008481526003602052604090205415610a8b57604080517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152601f60248201527f54686973206e6f646520697320616c7265616479207265676973746572656400604482015290519081900360640190fd5b61ffff80821690831610610b0057604080517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820181905260248201527f506f72742072616e676520697320656d707479206f7220696e636f7272656374604482015290519081900360640190fd5b6040805160c08101825285815267ffffffffffffffff198516602080830191825261ffff80871684860181815287831660608701908152608087019283526006805460a0890190815260008e8152600390975298862097518855955160018089018054945193519551871660e060020a027fffff0000ffffffffffffffffffffffffffffffffffffffffffffffffffffffff96881660d060020a027fffffffff0000ffffffffffffffffffffffffffffffffffffffffffffffffffff95891678010000000000000000000000000000000000000000000000000279ffff000000000000000000000000000000000000000000000000196801000000000000000090960477ffffffffffffffffffffffffffffffffffffffffffffffff199098169790971794909416959095179390931691909117939093169190911790559451600294850155835494850184559290527f405787fa12a823e0f2b7631cc41b3ba8828b3321ca811111fa75cd3aa3bb5ace90920186905581548484039091160190610c8b9082611b1a565b506040805185815290517fb0cd47a7093fb93a9ce97304d3afb8df43e02e48502e47fd5fbb6c4020d935b59181900360200190a15b610cc861118c565b15610cd257610cc0565b50505050565b600054600160a060020a031681565b6000610d1682604080519081016040528060098152602001600080516020611c5383398151915281525061068f565b92915050565b6060610d26611b3e565b506000828152600360209081526040808320815160c08101835281548152600182015467ffffffffffffffff19680100000000000000008202168286015261ffff78010000000000000000000000000000000000000000000000008204811683860181905260d060020a8304821660608581019190915260e060020a90930482166080850181905260029095015460a0850152855194031680845280860284019095019093529391928015610de5578160200160208202803883390190505b509150600090505b8151811015610e36576006818460a0015101815481101515610e0b57fe5b90600052602060002001548282815181101515610e2457fe5b60209081029091010152600101610ded565b509392505050565b60008060006060806060610e50611b73565b60608060606000610e5f611ba2565b60008d815260056020818152604080842081516080810183528154815282516060818101855260018401548252600284015482870152600384015460ff1682860152948201526004820154928101929092529092015490820152805190975011610f2a57604080517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152601860248201527f7468657265206973206e6f207375636820636c75737465720000000000000000604482015290519081900360640190fd5b85602001516040015160ff16604051908082528060200260200182016040528015610f5f578160200160208202803883390190505b50945085602001516040015160ff16604051908082528060200260200182016040528015610f97578160200160208202803883390190505b50935085602001516040015160ff16604051908082528060200260200182016040528015610fcf578160200160208202803883390190505b509250600091505b85602001516040015160ff168210156110c557600482876060015101815481101515610fff57fe5b600091825260209182902060408051808201909152600290920201805480835260019091015461ffff1692820192909252865190925086908490811061104157fe5b602090810290910181019190915281516000908152600390915260409020600101548451680100000000000000009091029085908490811061107f57fe5b67ffffffffffffffff19909216602092830290910182015281015183518490849081106110a857fe5b61ffff909216602092830290910190910152600190910190610fd7565b50506020808501518051910151604090950151909c949b50995091975095509350915050565b60008054600160a060020a0316331461110357600080fd5b5060005b815181101561066657611130828281518110151561112157fe5b906020019060200201516109b8565b600101611107565b600054600160a060020a0316331461114f57600080fd5b6108a28161190f565b611162828261116d565b151561066657600080fd5b600160a060020a03166000908152602091909152604090205460ff1690565b600080611197611bb9565b600080606080606060008060006111ac611b3e565b6111b4611ba2565b60009b505b6008548c101561120257600880548d9081106111d157fe5b600091825260209091206002600390920201810154905460ff909116116111f757611202565b8b6001019b506111b9565b6008548c106112145760009c5061171d565b600880548d90811061122257fe5b60009182526020918290206040805160608101825260039093029091018054835260018101549383019390935260029092015460ff16918101919091529a5061126a8c61198c565b600760008154809291906001019190505560010299504298506080604051908101604052808b6000191681526020018c81526020018a8152602001600480549050815250600560008c6000191660001916815260200190815260200160002060008201518160000190600019169055602082015181600101600082015181600001906000191690556020820151816001019060001916905560408201518160020160006101000a81548160ff021916908360ff160217905550505060408201518160040155606082015181600501559050508a6040015160ff1660405190808252806020026020018201604052801561136d578160200160208202803883390190505b5097508a6040015160ff166040519080825280602002602001820160405280156113a1578160200160208202803883390190505b5096508a6040015160ff166040519080825280602002602001820160405280156113d5578160200160208202803883390190505b50955060009450600093505b8a6040015160ff168410156115eb5760028054869081106113fe57fe5b6000918252602080832090910154808352600382526040808420815160c0810183528154815260018083015467ffffffffffffffff19680100000000000000008202168388015261ffff78010000000000000000000000000000000000000000000000008204811684870190815260d060020a83048216606086015260e060020a90920481166080850190815260029586015460a08601908152875180890190985288885281518316998801998a52600480549586018155909a528651939095027f8a35acfbc15ff81a39ae7d344fd709f28e8600b4aa8c65c6b64bfe7fe36bd19b81019390935596517f8a35acfbc15ff81a39ae7d344fd709f28e8600b4aa8c65c6b64bfe7fe36bd19c909201805492881661ffff1990931692909217909155519151955160068054959a509198509296508f9590949182169116919091010390811061154857fe5b6000918252602090912001558751839089908690811061156457fe5b6020908102909101810191909152820151875188908690811061158357fe5b67ffffffffffffffff19909216602092830290910182015281015186518790869081106115ac57fe5b61ffff9092166020928302909101909101526115c783611a14565b156115d7578460010194506115e0565b6115e085611a7a565b6001909301926113e1565b7f28c3d361196410d2059b40d53bf75ae21adebcec217c5a2564746ed2c3427fd28a8c600001518b8b8b8b6040518087600019166000191681526020018660001916600019168152602001858152602001806020018060200180602001848103845287818151815260200191508051906020019060200280838360005b83811015611680578181015183820152602001611668565b50505050905001848103835286818151815260200191508051906020019060200280838360005b838110156116bf5781810151838201526020016116a7565b50505050905001848103825285818151815260200191508051906020019060200280838360005b838110156116fe5781810151838201526020016116e6565b50505050905001995050505050505050505060405180910390a160019c505b50505050505050505050505090565b611796826001836040518082805190602001908083835b602083106117625780518252601f199092019160209182019101611743565b51815160209384036101000a6000190180199092169116179052920194855250604051938490030190922092915050611ad3565b81600160a060020a03167fd211483f91fc6eff862467f8de606587a30c8fc9981056f051b897a418df803a826040518080602001828103825283818151815260200191508051906020019080838360005b838110156117ff5781810151838201526020016117e7565b50505050905090810190601f16801561182c5780820380516001836020036101000a031916815260200191505b509250505060405180910390a25050565b6118a7826001836040518082805190602001908083835b602083106118735780518252601f199092019160209182019101611854565b51815160209384036101000a6000190180199092169116179052920194855250604051938490030190922092915050611af5565b81600160a060020a03167fbfec83d64eaa953f2708271a023ab9ee82057f8f3578d548c1a4ba0b5b70048982604051808060200182810382528381815181526020019150805190602001908083836000838110156117ff5781810151838201526020016117e7565b600160a060020a038116151561192457600080fd5b60008054604051600160a060020a03808516939216917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e091a36000805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a0392909216919091179055565b600854600019018114611a01576008805460001981019081106119ab57fe5b90600052602060002090600302016008828154811015156119c857fe5b600091825260209091208254600390920201908155600180830154908201556002918201549101805460ff191660ff9092169190911790555b6008805460001901906106669082611bd9565b6000908152600360205260409020600190810180547fffff0000ffffffffffffffffffffffffffffffffffffffffffffffffffffffff811660e060020a9182900461ffff90811690940184168202179182905560d060020a820483169104909116141590565b600254600019018114611ac057600280546000198101908110611a9957fe5b9060005260206000200154600282815481101515611ab357fe5b6000918252602090912001555b6002805460001901906106669082611b1a565b600160a060020a0316600090815260209190915260409020805460ff19169055565b600160a060020a0316600090815260209190915260409020805460ff19166001179055565b81548183558181111561080c5760008381526020902061080c918101908301611c05565b6040805160c081018252600080825260208201819052918101829052606081018290526080810182905260a081019190915290565b6040805160c081019091526000815260208101611b8e611bb9565b815260200160008152602001600081525090565b604080518082019091526000808252602082015290565b604080516060810182526000808252602082018190529181019190915290565b81548183558181111561080c5760030281600302836000526020600020918201910161080c9190611c26565b611c2391905b80821115611c1f5760008155600101611c0b565b5090565b90565b611c2391905b80821115611c1f576000808255600182015560028101805460ff19169055600301611c2c560077686974656c6973740000000000000000000000000000000000000000000000a165627a7a72305820ecbf45aa2b3f88ada94397bce877e7633711db9c28ccf86c23830f59106d49fb0029";

    public static final String FUNC_CHECKROLE = "checkRole";

    public static final String FUNC_ROLE_WHITELISTED = "ROLE_WHITELISTED";

    public static final String FUNC_HASROLE = "hasRole";

    public static final String FUNC_ADDCODE = "addCode";

    public static final String FUNC_REMOVEADDRESSESFROMWHITELIST = "removeAddressesFromWhitelist";

    public static final String FUNC_REMOVEADDRESSFROMWHITELIST = "removeAddressFromWhitelist";

    public static final String FUNC_GETSTATUS = "getStatus";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_ADDADDRESSTOWHITELIST = "addAddressToWhitelist";

    public static final String FUNC_ADDNODE = "addNode";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_WHITELIST = "whitelist";

    public static final String FUNC_GETNODECLUSTERS = "getNodeClusters";

    public static final String FUNC_GETCLUSTER = "getCluster";

    public static final String FUNC_ADDADDRESSESTOWHITELIST = "addAddressesToWhitelist";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event CLUSTERFORMED_EVENT = new Event("ClusterFormed", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<DynamicArray<Bytes32>>() {}, new TypeReference<DynamicArray<Bytes24>>() {}, new TypeReference<DynamicArray<Uint16>>() {}));
    ;

    public static final Event CODEENQUEUED_EVENT = new Event("CodeEnqueued", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    ;

    public static final Event NEWNODE_EVENT = new Event("NewNode", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    ;

    public static final Event ROLEADDED_EVENT = new Event("RoleAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event ROLEREMOVED_EVENT = new Event("RoleRemoved", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event OWNERSHIPRENOUNCED_EVENT = new Event("OwnershipRenounced", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    @Deprecated
    protected Deployer(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Deployer(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Deployer(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Deployer(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public void checkRole(Address _operator, Utf8String _role) {
        throw new RuntimeException("cannot call constant function with void return type");
    }

    public RemoteCall<Utf8String> ROLE_WHITELISTED() {
        final Function function = new Function(FUNC_ROLE_WHITELISTED, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Bool> hasRole(Address _operator, Utf8String _role) {
        final Function function = new Function(FUNC_HASROLE, 
                Arrays.<Type>asList(_operator, _role), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<TransactionReceipt> addCode(Bytes32 storageHash, Bytes32 storageReceipt, Uint8 clusterSize) {
        final Function function = new Function(
                FUNC_ADDCODE, 
                Arrays.<Type>asList(storageHash, storageReceipt, clusterSize), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> removeAddressesFromWhitelist(DynamicArray<Address> _operators) {
        final Function function = new Function(
                FUNC_REMOVEADDRESSESFROMWHITELIST, 
                Arrays.<Type>asList(_operators), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> removeAddressFromWhitelist(Address _operator) {
        final Function function = new Function(
                FUNC_REMOVEADDRESSFROMWHITELIST, 
                Arrays.<Type>asList(_operator), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Tuple3<Uint8, Uint256, DynamicArray<Uint256>>> getStatus() {
        final Function function = new Function(FUNC_GETSTATUS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Uint256>() {}, new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteCall<Tuple3<Uint8, Uint256, DynamicArray<Uint256>>>(
                new Callable<Tuple3<Uint8, Uint256, DynamicArray<Uint256>>>() {
                    @Override
                    public Tuple3<Uint8, Uint256, DynamicArray<Uint256>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<Uint8, Uint256, DynamicArray<Uint256>>(
                                (Uint8) results.get(0), 
                                (Uint256) results.get(1), 
                                (DynamicArray<Uint256>) results.get(2));
                    }
                });
    }

    public RemoteCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> addAddressToWhitelist(Address _operator) {
        final Function function = new Function(
                FUNC_ADDADDRESSTOWHITELIST, 
                Arrays.<Type>asList(_operator), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> addNode(Bytes32 nodeID, Bytes24 nodeAddress, Uint16 startPort, Uint16 endPort) {
        final Function function = new Function(
                FUNC_ADDNODE, 
                Arrays.<Type>asList(nodeID, nodeAddress, startPort, endPort), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Address> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Bool> whitelist(Address _operator) {
        final Function function = new Function(FUNC_WHITELIST, 
                Arrays.<Type>asList(_operator), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<DynamicArray<Bytes32>> getNodeClusters(Bytes32 nodeID) {
        final Function function = new Function(FUNC_GETNODECLUSTERS, 
                Arrays.<Type>asList(nodeID), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<Tuple6<Bytes32, Bytes32, Uint256, DynamicArray<Bytes32>, DynamicArray<Bytes24>, DynamicArray<Uint16>>> getCluster(Bytes32 clusterID) {
        final Function function = new Function(FUNC_GETCLUSTER, 
                Arrays.<Type>asList(clusterID), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<DynamicArray<Bytes32>>() {}, new TypeReference<DynamicArray<Bytes24>>() {}, new TypeReference<DynamicArray<Uint16>>() {}));
        return new RemoteCall<Tuple6<Bytes32, Bytes32, Uint256, DynamicArray<Bytes32>, DynamicArray<Bytes24>, DynamicArray<Uint16>>>(
                new Callable<Tuple6<Bytes32, Bytes32, Uint256, DynamicArray<Bytes32>, DynamicArray<Bytes24>, DynamicArray<Uint16>>>() {
                    @Override
                    public Tuple6<Bytes32, Bytes32, Uint256, DynamicArray<Bytes32>, DynamicArray<Bytes24>, DynamicArray<Uint16>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple6<Bytes32, Bytes32, Uint256, DynamicArray<Bytes32>, DynamicArray<Bytes24>, DynamicArray<Uint16>>(
                                (Bytes32) results.get(0), 
                                (Bytes32) results.get(1), 
                                (Uint256) results.get(2), 
                                (DynamicArray<Bytes32>) results.get(3), 
                                (DynamicArray<Bytes24>) results.get(4), 
                                (DynamicArray<Uint16>) results.get(5));
                    }
                });
    }

    public RemoteCall<TransactionReceipt> addAddressesToWhitelist(DynamicArray<Address> _operators) {
        final Function function = new Function(
                FUNC_ADDADDRESSESTOWHITELIST, 
                Arrays.<Type>asList(_operators), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> transferOwnership(Address _newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(_newOwner), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public List<ClusterFormedEventResponse> getClusterFormedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(CLUSTERFORMED_EVENT, transactionReceipt);
        ArrayList<ClusterFormedEventResponse> responses = new ArrayList<ClusterFormedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ClusterFormedEventResponse typedResponse = new ClusterFormedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.clusterID = (Bytes32) eventValues.getNonIndexedValues().get(0);
            typedResponse.storageHash = (Bytes32) eventValues.getNonIndexedValues().get(1);
            typedResponse.genesisTime = (Uint256) eventValues.getNonIndexedValues().get(2);
            typedResponse.solverIDs = (DynamicArray<Bytes32>) eventValues.getNonIndexedValues().get(3);
            typedResponse.solverAddrs = (DynamicArray<Bytes24>) eventValues.getNonIndexedValues().get(4);
            typedResponse.solverPorts = (DynamicArray<Uint16>) eventValues.getNonIndexedValues().get(5);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ClusterFormedEventResponse> clusterFormedEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, ClusterFormedEventResponse>() {
            @Override
            public ClusterFormedEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(CLUSTERFORMED_EVENT, log);
                ClusterFormedEventResponse typedResponse = new ClusterFormedEventResponse();
                typedResponse.log = log;
                typedResponse.clusterID = (Bytes32) eventValues.getNonIndexedValues().get(0);
                typedResponse.storageHash = (Bytes32) eventValues.getNonIndexedValues().get(1);
                typedResponse.genesisTime = (Uint256) eventValues.getNonIndexedValues().get(2);
                typedResponse.solverIDs = (DynamicArray<Bytes32>) eventValues.getNonIndexedValues().get(3);
                typedResponse.solverAddrs = (DynamicArray<Bytes24>) eventValues.getNonIndexedValues().get(4);
                typedResponse.solverPorts = (DynamicArray<Uint16>) eventValues.getNonIndexedValues().get(5);
                return typedResponse;
            }
        });
    }

    public Observable<ClusterFormedEventResponse> clusterFormedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CLUSTERFORMED_EVENT));
        return clusterFormedEventObservable(filter);
    }

    public List<CodeEnqueuedEventResponse> getCodeEnqueuedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(CODEENQUEUED_EVENT, transactionReceipt);
        ArrayList<CodeEnqueuedEventResponse> responses = new ArrayList<CodeEnqueuedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CodeEnqueuedEventResponse typedResponse = new CodeEnqueuedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.storageHash = (Bytes32) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<CodeEnqueuedEventResponse> codeEnqueuedEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, CodeEnqueuedEventResponse>() {
            @Override
            public CodeEnqueuedEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(CODEENQUEUED_EVENT, log);
                CodeEnqueuedEventResponse typedResponse = new CodeEnqueuedEventResponse();
                typedResponse.log = log;
                typedResponse.storageHash = (Bytes32) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Observable<CodeEnqueuedEventResponse> codeEnqueuedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CODEENQUEUED_EVENT));
        return codeEnqueuedEventObservable(filter);
    }

    public List<NewNodeEventResponse> getNewNodeEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(NEWNODE_EVENT, transactionReceipt);
        ArrayList<NewNodeEventResponse> responses = new ArrayList<NewNodeEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            NewNodeEventResponse typedResponse = new NewNodeEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (Bytes32) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<NewNodeEventResponse> newNodeEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, NewNodeEventResponse>() {
            @Override
            public NewNodeEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(NEWNODE_EVENT, log);
                NewNodeEventResponse typedResponse = new NewNodeEventResponse();
                typedResponse.log = log;
                typedResponse.id = (Bytes32) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Observable<NewNodeEventResponse> newNodeEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(NEWNODE_EVENT));
        return newNodeEventObservable(filter);
    }

    public List<RoleAddedEventResponse> getRoleAddedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(ROLEADDED_EVENT, transactionReceipt);
        ArrayList<RoleAddedEventResponse> responses = new ArrayList<RoleAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RoleAddedEventResponse typedResponse = new RoleAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.operator = (Address) eventValues.getIndexedValues().get(0);
            typedResponse.role = (Utf8String) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<RoleAddedEventResponse> roleAddedEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, RoleAddedEventResponse>() {
            @Override
            public RoleAddedEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(ROLEADDED_EVENT, log);
                RoleAddedEventResponse typedResponse = new RoleAddedEventResponse();
                typedResponse.log = log;
                typedResponse.operator = (Address) eventValues.getIndexedValues().get(0);
                typedResponse.role = (Utf8String) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Observable<RoleAddedEventResponse> roleAddedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ROLEADDED_EVENT));
        return roleAddedEventObservable(filter);
    }

    public List<RoleRemovedEventResponse> getRoleRemovedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(ROLEREMOVED_EVENT, transactionReceipt);
        ArrayList<RoleRemovedEventResponse> responses = new ArrayList<RoleRemovedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RoleRemovedEventResponse typedResponse = new RoleRemovedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.operator = (Address) eventValues.getIndexedValues().get(0);
            typedResponse.role = (Utf8String) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<RoleRemovedEventResponse> roleRemovedEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, RoleRemovedEventResponse>() {
            @Override
            public RoleRemovedEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(ROLEREMOVED_EVENT, log);
                RoleRemovedEventResponse typedResponse = new RoleRemovedEventResponse();
                typedResponse.log = log;
                typedResponse.operator = (Address) eventValues.getIndexedValues().get(0);
                typedResponse.role = (Utf8String) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Observable<RoleRemovedEventResponse> roleRemovedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ROLEREMOVED_EVENT));
        return roleRemovedEventObservable(filter);
    }

    public List<OwnershipRenouncedEventResponse> getOwnershipRenouncedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OWNERSHIPRENOUNCED_EVENT, transactionReceipt);
        ArrayList<OwnershipRenouncedEventResponse> responses = new ArrayList<OwnershipRenouncedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipRenouncedEventResponse typedResponse = new OwnershipRenouncedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (Address) eventValues.getIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<OwnershipRenouncedEventResponse> ownershipRenouncedEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, OwnershipRenouncedEventResponse>() {
            @Override
            public OwnershipRenouncedEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OWNERSHIPRENOUNCED_EVENT, log);
                OwnershipRenouncedEventResponse typedResponse = new OwnershipRenouncedEventResponse();
                typedResponse.log = log;
                typedResponse.previousOwner = (Address) eventValues.getIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Observable<OwnershipRenouncedEventResponse> ownershipRenouncedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPRENOUNCED_EVENT));
        return ownershipRenouncedEventObservable(filter);
    }

    public List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (Address) eventValues.getIndexedValues().get(0);
            typedResponse.newOwner = (Address) eventValues.getIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<OwnershipTransferredEventResponse> ownershipTransferredEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, OwnershipTransferredEventResponse>() {
            @Override
            public OwnershipTransferredEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
                OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
                typedResponse.log = log;
                typedResponse.previousOwner = (Address) eventValues.getIndexedValues().get(0);
                typedResponse.newOwner = (Address) eventValues.getIndexedValues().get(1);
                return typedResponse;
            }
        });
    }

    public Observable<OwnershipTransferredEventResponse> ownershipTransferredEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventObservable(filter);
    }

    public static RemoteCall<Deployer> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Deployer.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Deployer> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Deployer.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<Deployer> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Deployer.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Deployer> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Deployer.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static Deployer load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Deployer(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Deployer load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Deployer(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Deployer load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Deployer(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Deployer load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Deployer(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class ClusterFormedEventResponse {
        public Log log;

        public Bytes32 clusterID;

        public Bytes32 storageHash;

        public Uint256 genesisTime;

        public DynamicArray<Bytes32> solverIDs;

        public DynamicArray<Bytes24> solverAddrs;

        public DynamicArray<Uint16> solverPorts;
    }

    public static class CodeEnqueuedEventResponse {
        public Log log;

        public Bytes32 storageHash;
    }

    public static class NewNodeEventResponse {
        public Log log;

        public Bytes32 id;
    }

    public static class RoleAddedEventResponse {
        public Log log;

        public Address operator;

        public Utf8String role;
    }

    public static class RoleRemovedEventResponse {
        public Log log;

        public Address operator;

        public Utf8String role;
    }

    public static class OwnershipRenouncedEventResponse {
        public Log log;

        public Address previousOwner;
    }

    public static class OwnershipTransferredEventResponse {
        public Log log;

        public Address previousOwner;

        public Address newOwner;
    }
}
