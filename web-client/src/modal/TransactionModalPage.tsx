import { useState } from 'react';
import styles from './TransactionModalPage.module.css'

import dropDownIcon from '../assets/icons/drop_down_icon.png';
import { ModalPage } from './ModalPage';

interface TransactionModalPageProps {
      transaction : any;
      onClose : () => void;
}

function getColor(transactionStatus : string) : string[]{
      switch(transactionStatus){
            case "CONFIRMED":
                  return ["#078f00", "#FFFFFF"];
            case "PENDING":
                  return ["#e6d417", "#000000"];
            case "REJECTED":
                  return ["#a31c1c", "#FFFFFF"];
            default:
                  return ["#1677ab", "#FFFFFF"];
      }
}

export const TransactionModalPage: React.FC<TransactionModalPageProps> = ({ transaction, onClose}) => {
      const [showFullReceiverKey, setShowFullReceiverKey] = useState<boolean>(false);
      const [showFullSenderKey, setShowFullSenderKey] = useState<boolean>(false);
      const [showFullSignature, setShowFullSignature] = useState<boolean>(false);
      const [showInputs, setShowInputs] = useState<boolean>(true);
      const [showOutputs, setShowOutputs] = useState<boolean>(true);

      return (
            <ModalPage onClose={() => {onClose();}} className={styles.modalPage}>
                  <span className={styles.title}>Transaction Details:</span>
                  <div className={styles.section}>
                        <div className={styles.contentElement}>
                              <span>Hash: </span>
                              <span>{transaction.transactionId}</span>
                        </div>
                        <div className={styles.contentElement}>
                              <span>Status: </span>
                              <span className={styles.status} style={{backgroundColor: getColor(transaction.status)[0], color: getColor(transaction.status)[1]}}> {transaction.status}</span>
                        </div>
                        <div className={styles.contentElement}>
                              <span>Date: </span>
                              <span>{new Date(transaction.timeStamp).toLocaleString()}</span>
                        </div>
                  </div>
                  <div className={styles.section}>
                        <div className={styles.contentElement}>
                              <span>Amount: </span>
                              <span>{transaction.amount / 100_000_000} coins</span>
                        </div>
                        <div className={`${styles.contentElement}`}>
                              <span>Sender: </span>
                              {!showFullSenderKey && <span className={styles.addressSpan} onClick={() => {setShowFullSenderKey(!showFullSenderKey)}}>{transaction.senderPublicKey.length >= 54 ? `${transaction.senderPublicKey.substring(0, 27)}...${transaction.senderPublicKey.substring(transaction.senderPublicKey.length - 27, transaction.senderPublicKey.length)}` : transaction.senderPublicKey || "None"}</span>}
                              {showFullSenderKey && <span className={styles.addressSpan} onClick={() => {setShowFullSenderKey(!showFullSenderKey)}}>{transaction.senderPublicKey || "Unknown"}</span>}

                        </div>
                        <div className={`${styles.contentElement}`}>
                              <span>Receiver: </span>
                              {!showFullReceiverKey && <span className={styles.addressSpan} onClick={() => {setShowFullReceiverKey(!showFullReceiverKey)}}>{transaction.receiverPublicKey.length >= 54 ? `${transaction.receiverPublicKey.substring(0, 27)}...${transaction.receiverPublicKey.substring(transaction.receiverPublicKey.length - 27, transaction.receiverPublicKey.length)}` : transaction.receiverPublicKey}</span>}
                              {showFullReceiverKey && <span className={styles.addressSpan} onClick={() => {setShowFullReceiverKey(!showFullReceiverKey)}}>{transaction.receiverPublicKey}</span>}
                        </div>
                  </div>
                  <div className={styles.section}>
                        <div className={`${styles.contentElement} ${styles.verticalAlignCenter}`}>
                              <span>Inputs:</span>
                              <span>({transaction.inputs ? transaction.inputs.length : "0"})</span>
                              <img className={styles.showMoreIcon} src={dropDownIcon} onClick={() => {setShowInputs(!showInputs)}}></img>
                        </div>      
                        {showInputs && transaction.inputs && transaction.inputs.length > 0 && (
                              <table className={styles.inputTable}>
                                    <thead>
                                          <tr>
                                                <th>Transaction Id</th>
                                                <th>Output Index</th>
                                                <th>Owner</th>
                                          </tr>
                                    </thead>
                                    <tbody>
                                          {transaction.inputs && Object.keys(transaction.inputs).length > 0 && Object.entries(transaction.inputs).map(([key, value]) => {
                                                const utxoData: string[] = (value as string).split(":");
                                                return (
                                                      <tr key={key}>
                                                            <td>{utxoData[1]}</td>
                                                            <td>{utxoData[2]}</td>
                                                            <td>{utxoData[0]}</td>
                                                            {/* <td>{`${utxoData[0].substring(0, 16)}...${utxoData[0].substring(utxoData[0].length - 16, utxoData[0].length)}`}</td> */}
                                                      </tr>
                                                );
                                          })}
                                    </tbody>
                              </table>
                        )}
                        <div className={`${styles.contentElement} ${styles.verticalAlignCenter}`}>
                              <span>Outputs:</span>
                              <span>({transaction.outputs ? transaction.outputs.length : "0"})</span>
                              <img className={styles.showMoreIcon} src={dropDownIcon} onClick={() => {setShowOutputs(!showOutputs)}}></img>
                        </div>
                        {showOutputs && transaction.outputs && transaction.outputs.length > 0 && (
                              <table className={styles.inputTable}>
                                    <thead>
                                          <tr>
                                                <th>Output Index</th>
                                                <th>Amount</th>
                                                <th>Owner</th>
                                          </tr>
                                    </thead>
                                    <tbody>
                                          {transaction.outputs && Object.keys(transaction.outputs).length > 0 && Object.entries(transaction.outputs).map(([key, value]) => {
                                                const output = value as { outputIndex: number; amount: number; owner: string };
                                                return (
                                                      <tr key={key}>
                                                            <td>{output.outputIndex}</td>
                                                            <td>{output.amount / 100_000_000}</td>
                                                            <td>{output.owner}</td>
                                                      </tr>
                                                );
                                          })}
                                    </tbody>
                              </table>
                        )}
                        <div className={styles.contentElement}>
                              <span>Fee: </span>
                              <span>{transaction.transactionFee / 100_000_000} coins</span>
                        </div>
                        <div className={styles.contentElement}>
                              <span>Signature: </span>
                              {!showFullSignature && <span className={styles.addressSpan} onClick={() => {setShowFullSignature(!showFullSignature)}}>{transaction.digitalSignature && transaction.digitalSignature.length >= 54 ? `${transaction.digitalSignature.substring(0, 27)}...${transaction.digitalSignature.substring(transaction.digitalSignature.length - 27, transaction.digitalSignature.length)}` : transaction.digitalSignature || "Unsigned"}</span>}
                              {showFullSignature && <span className={styles.addressSpan} onClick={() => {setShowFullSignature(!showFullSignature)}}>{transaction.digitalSignature || "Unsigned"}</span>}
                        </div>
                  </div>
                  <div className={styles.emptyBlock}></div>
            </ModalPage>
      );
}