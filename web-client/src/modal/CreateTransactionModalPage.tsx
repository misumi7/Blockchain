import { useRef, useState } from 'react';
import styles from './CreateTransactionModalPage.module.css'
import { ModalPage } from './ModalPage';
import { Button } from '../Button';
import { PinInput } from '../PinInput';

import processTransactionIcon from '../assets/icons/transaction_processing.gif'
import sendIcon from '../assets/icons/send_transaction_icon.png'
import axios from 'axios';
import { getInputsRequired, getRecommendedFee } from '../service/TransactionService';
import { time, timeStamp } from 'console';
import { sign } from 'crypto';
import { getTransactionHash, signTransactionHash } from '../utils/WalletUtils';
import { Wallet } from '../types/Wallet';

// import argon2 from 'argon2-wasm';
// const argon2Inst = await argon2.Argon2Browser();


interface CreateTransactionModalPage{
      onClose : () => void;
      onSent : () => void;
      walletPublicKey: string;
      wallet : Wallet;
}

function validatePin(pinValues : string[]) : boolean {
      return pinValues.length > 0 && pinValues.every(value => value != '');
}

// async function deriveKey(pinValues : string[], salt: Uint8Array) : Promise<Uint8Array> {
//       const result = await argon2Inst.hash({
//             pass: pinValues.join(''),
//             salt: salt,
//             time: 2,
//             mem: 65536,
//             parallelism: 1,
//             type: argon2.ArgonType.Argon2id,
//             hashLen: 32
//       });
//       return result.hash;
// }

async function importRsaPublicKey(rawKey : Uint8Array) : Promise<CryptoKey>{
      return crypto.subtle.importKey(
            "spki",
            rawKey.buffer,
            {
                  name: "RSA-OAEP",
                  hash: "SHA-256"
            },
            false, ["encrypt"]
      );
}

async function encryptKey(pinValues : string[], walletPublicKey : string) : Promise<string> {
      // const response = await axios.get('/api/wallets/salt', {
      //       params: { walletPublicKey },
      //       responseType: 'arraybuffer'
      // });
      // const salt = new Uint8Array(response.data as ArrayBuffer);
      // const aesKey = await deriveKey(pinValues, salt);

      const responseRsaPublicKey = await axios.get<ArrayBuffer>('/api/wallets/rsa-public-key', {
            responseType: 'arraybuffer'
      });
      const rawRsaPublicKey = new Uint8Array(responseRsaPublicKey.data);
      const rsaPublicKey = await importRsaPublicKey(rawRsaPublicKey);

      const encodedPin = new TextEncoder().encode(pinValues.join(''));
      // @ts-ignore
      const encryptedAesKey = await crypto.subtle.encrypt({ name: "RSA-OAEP", hash: "SHA-256" }, rsaPublicKey, encodedPin);
      console.log(`Encrypted pin: ${btoa(String.fromCharCode(...new Uint8Array(encryptedAesKey)))}`)
      return btoa(String.fromCharCode(...new Uint8Array(encryptedAesKey)));
}

export const CreateTransactionModalPage : React.FC<CreateTransactionModalPage> = ({ onClose, walletPublicKey, onSent, wallet }) => {
      const [playProcessingAnimation, setPlayAnimation] = useState(false);
      const [pinValues, setPinValues] = useState<string[]>([]);
      const [wasTransactionAccepted, setWasTransactionAccepted] = useState<boolean>(); 

      const senderRef = useRef<HTMLInputElement>(null);
      const receiverRef = useRef<HTMLInputElement>(null);
      const amountRef = useRef<HTMLInputElement>(null);

      const sendTransaction = async () => {
            // console.log(senderRef.current?.value);
            // console.log(receiverRef.current?.value);
            // console.log(amountRef.current?.value);
            // console.log(`${pinValues}\n`);

            if(!validatePin(pinValues)){
                  return;
            }
            
            const fee = await getInputsRequired(walletPublicKey, Number(amountRef.current?.value)) * await getRecommendedFee();
            console.log(`Fee: ${fee}`);
            const timeStamp = Date.now();

            const transactionHash = getTransactionHash(
                  senderRef.current?.value || '',
                  receiverRef.current?.value || '',
                  Number(amountRef.current?.value) || 0,
                  fee,
                  timeStamp
            );

            await axios.post('/api/transactions/create-signed', {
                  fee: fee,
                  senderPublicKey: senderRef.current?.value,
                  receiverPublicKey: receiverRef.current?.value,
                  amount: amountRef.current?.value,
                  timeStamp: timeStamp,
                  signature: await signTransactionHash(transactionHash, wallet.encryptedPrivateKey, pinValues.join(''), wallet.salt, wallet.iv)       
            })
            .then(response => {
                  setWasTransactionAccepted(response.status == 200);
                  onSent();
            }).catch(() => {
                  setWasTransactionAccepted(false);
                  onSent();
            });

            setTimeout(() => setWasTransactionAccepted(undefined), 1000);

            //setTransactions(transactions.data);
      };

      return (
            <ModalPage onClose={onClose} className={styles.createTransactionPage}>
                  <span className={styles.title}>New Transaction</span>
                  <div className={styles.inputBox}>
                        <label htmlFor='senderPublicKey'>Sender</label>
                        <input ref={senderRef} name='senderPublicKey' value={walletPublicKey} readOnly></input>
                  </div>
                  <div className={styles.inputBox}>
                        <label htmlFor='receiverPublicKey'>Receiver</label>
                        <input ref={receiverRef} name='receiverPublicKey'></input>
                  </div>
                  <div className={styles.inputBox}>
                        <label htmlFor='amount'>Amount</label>
                        <input ref={amountRef} name='amount'></input>
                  </div>
                  <PinInput setPinValues={setPinValues}/>
                  <Button 
                        text={'Send'} 
                        isActive={validatePin(pinValues)} 
                        hideText={wasTransactionAccepted != undefined}
                        onClick={() => {
                              setPlayAnimation(true);
                              sendTransaction();
                              setPlayAnimation(false);
                        }} 
                        icon={playProcessingAnimation ? processTransactionIcon : sendIcon} 
                        className={`${styles.sendButton} ${
                              wasTransactionAccepted == true ? styles.successAnimation
                              : wasTransactionAccepted == false ? styles.failureAnimation : ''
                        }`}/>
            </ModalPage>
      );
}