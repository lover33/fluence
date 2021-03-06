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

use contract_status::code::Code;
use std::error::Error;
use types::NodeAddress;
use utils;
use web3::types::{Address, H256, U256};

#[derive(Serialize, Deserialize, Debug, Getters)]
pub struct ClusterMember {
    id: H256,
    tendermint_key: String,
    ip_addr: String,
    port: u16,
    owner: Address,
}

impl ClusterMember {
    pub fn new(
        id: H256,
        address: NodeAddress,
        port: u16,
        owner: Address,
    ) -> Result<ClusterMember, Box<Error>> {
        let (tendermint_key, ip_addr) = address.decode()?;
        Ok(ClusterMember {
            id,
            tendermint_key,
            ip_addr,
            port,
            owner,
        })
    }
}

#[derive(Serialize, Deserialize, Debug, Getters)]
pub struct Cluster {
    id: H256,
    genesis_time: U256,
    code: Code,
    cluster_members: Vec<ClusterMember>,
}

impl Cluster {
    pub fn new(
        id: H256,
        genesis_time: U256,
        code: Code,
        cluster_members: Vec<ClusterMember>,
    ) -> Cluster {
        Cluster {
            id,
            genesis_time,
            code,
            cluster_members,
        }
    }
}

/// Gets list of formed clusters from Fluence contract
pub fn get_clusters(contract_address: Address, eth_url: &str) -> Result<Vec<Cluster>, Box<Error>> {
    let options = utils::options();

    let (cluster_ids, genesis_times, code_addresses, storage_receipts, cluster_sizes, developers): (
        Vec<H256>,
        Vec<U256>,
        Vec<H256>,
        Vec<H256>,
        Vec<u64>,
        Vec<Address>,
    ) = utils::query_contract(
        contract_address,
        eth_url,
        "getClustersInfo",
        (),
        options.to_owned(),
    )?;

    let (nodes_ids, nodes_addresses, ports, owners): (
        Vec<H256>,
        Vec<NodeAddress>,
        Vec<u64>,
        Vec<Address>,
    ) = utils::query_contract(
        contract_address,
        eth_url,
        "getClustersNodes",
        (),
        options.to_owned(),
    )?;

    let mut clusters: Vec<Cluster> = Vec::new();
    let mut nodes_counter = 0;

    for i in 0..code_addresses.len() {
        let cluster_size = cluster_sizes[i];

        let mut cluster_members: Vec<ClusterMember> = Vec::new();

        for _j in 0..cluster_size {
            let id = nodes_ids[nodes_counter];
            let address = nodes_addresses[nodes_counter];
            let port = ports[nodes_counter] as u16;
            let owner = owners[nodes_counter];

            let cluster_member = ClusterMember::new(id, address, port, owner)?;

            cluster_members.push(cluster_member);

            nodes_counter = nodes_counter + 1;
        }

        let code = Code::new(
            code_addresses[i],
            storage_receipts[i],
            cluster_sizes[i] as u8,
            developers[i],
        );

        let cluster = Cluster::new(cluster_ids[i], genesis_times[i], code, cluster_members);

        clusters.push(cluster);
    }

    Ok(clusters)
}
