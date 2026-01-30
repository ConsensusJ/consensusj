/*
 * Copyright 2014-2026 ConsensusJ Developers.
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
package org.consensusj.bitcoinj.service;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoinj.signing.SigningRequest;
import org.consensusj.bitcoinj.signing.TransactionInputData;
import org.consensusj.bitcoinj.signing.TransactionOutputData;
import org.consensusj.bitcoinj.signing.TransactionSigner;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A wallet-level service that can complete and sign transactions.
 */
public interface SignTransactionService extends TransactionSigner {

    /**
     * Create and sign a transaction to send coins to the specified address. Implements the transaction-building
     * and signing portion of {@code sendtoaddress} RPC.
     * @param toAddress destination address
     * @param amount amount to send
     * @return a future signed transaction
     */
    CompletableFuture<Transaction> signSendToAddress(Address toAddress, Coin amount) throws IOException, InsufficientMoneyException;

    /**
     * Create a signing request, given a list of inputs, a list of outputs and a change address. Calculates
     * the fee, adds a change output, and returns a completed signing request.
     * @param inputUtxos list of available UTXOs
     * @param outputs Outputs to send to
     * @param changeAddress address that will receive change
     * @return a completed transaction signing request
     */
    SigningRequest createBitcoinSigningRequest(List<TransactionInputData> inputUtxos, List<TransactionOutputData> outputs, Address changeAddress) throws InsufficientMoneyException;
}
