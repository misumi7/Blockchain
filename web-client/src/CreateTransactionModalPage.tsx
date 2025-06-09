import { useRef, useState } from 'react';
import styles from './CreateTransactionModalPage.module.css'
import { ModalPage } from './ModalPage';
import { Button } from './Button';
import { PinInput } from './PinInput';

import processTransactionIcon from './assets/icons/transaction_processing.gif'
import sendIcon from './assets/icons/send_transaction_icon.png'
import axios from 'axios';



interface CreateTransactionModalPage{
      onClose : () => void;
      walletPublicKey: string;
}

function validatePin(pinValues : string[]) : boolean {
      return pinValues.length > 0 && pinValues.every(value => value != '');
}

export const CreateTransactionModalPage : React.FC<CreateTransactionModalPage> = ({ onClose, walletPublicKey }) => {
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
            
            await axios.post('/api/transactions/create', {
                  senderPublicKey: senderRef.current?.value,
                  receiverPublicKey: receiverRef.current?.value,
                  amount: amountRef.current?.value,
                  encryptedPin: pinValues.join('')
            })
            .then(response => {
                  setWasTransactionAccepted(response.status == 200);
            }).catch(() => {
                  setWasTransactionAccepted(false);
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