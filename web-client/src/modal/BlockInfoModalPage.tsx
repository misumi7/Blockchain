import styles from './BlockInfoModalPage.module.css'
import { ModalPage } from './ModalPage';
import dropDownIcon from '../assets/icons/drop_down_icon.png'
import { useState } from 'react';

interface BlockInfoModalPageProps {
      blockData : any;
      onClose : () => void;
}

export const BlockInfoModalPage : React.FC<BlockInfoModalPageProps> = ({ blockData, onClose }) => {
      const [showTransactions, setShowTransactions] = useState<boolean>(true);
      
      return (
            <ModalPage className={styles.blockInfo} onClose={onClose}>
                  <span className={styles.title}>Block #{blockData.index}</span>
                  <div className={styles.infoBox}>
                        <div className={styles.section}>
                              <div className={styles.contentElement}>
                                    <span>Hash: </span>
                                    <span>{blockData.blockHash}</span>
                              </div>
                              <div className={styles.contentElement}>
                                    <span>Previous Hash: </span>
                                    <span>{blockData.previousHash}</span>
                              </div>
                              <div className={styles.contentElement}>
                                    <span>Date: </span>
                                    <span>{new Date(blockData.timeStamp).toLocaleString()}</span>
                              </div>
                        </div>
                        <div className={styles.section}>
                              <div className={`${styles.contentElement} ${styles.verticalAlignCenter}`}>
                                    <span>Transactions:</span>
                                    <span>({blockData.transactions ? blockData.transactions.length : "0"})</span>
                                    <img className={styles.showMoreIcon} src={dropDownIcon} onClick={() => {setShowTransactions(!showTransactions)}}></img>
                              </div>  
                              {showTransactions && blockData.transactions && blockData.transactions.length > 0 && (
                              <table className={styles.inputTable}>
                                    <thead>
                                          <tr>
                                                <th>Transaction Id</th>
                                                <th>Amount</th>
                                          </tr>
                                    </thead>
                                    <tbody>
                                          {blockData.transactions && Object.keys(blockData.transactions).length > 0 && Object.entries(blockData.transactions).map(([key, value]) => {
                                                return (
                                                      <tr key={key}>
                                                            <td>{(value as {transactionId : string}).transactionId}</td>
                                                            <td>{(value as { amount: number }).amount / 100_000_000} coins</td>
                                                      </tr>
                                                );
                                          })}
                                    </tbody>
                              </table>
                        )}
                        </div>
                  </div>
            </ModalPage>
      );
}