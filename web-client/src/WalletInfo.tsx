import { useEffect, useRef, useState } from 'react';
import styles from './WalletInfo.module.css'

import infoIcon from './assets/icons/info_icon.png';
import coinIcon from './assets/icons/coin_icon.png';
import copyIcon from './assets/icons/copy_icon.png';
import noTransactionsIcon from './assets/icons/no_transactions_icon.png';
import { Button } from './Button';

import axios from 'axios';
import { TransactionModalPage } from './modal/TransactionModalPage'
import { CreateTransactionModalPage } from './modal/CreateTransactionModalPage';

interface WalletInfoProps {
      walletPublicKey : string;
}

export const WalletInfo : React.FC<WalletInfoProps> = ({ walletPublicKey }) => {
      const [wasCopied, setCopied] = useState(false);
      const pkInputRef = useRef<HTMLInputElement>(null);
      const copyPublicKey = () => {
            if(pkInputRef.current){
                  pkInputRef.current.select();
                  navigator.clipboard.writeText(pkInputRef.current.value).then(() => {
                        setCopied(true);
                        setTimeout(() => {
                              setCopied(false);
                        }, 1000);
                  });
            }
      }

      const [modalPageData, setShowModalPage] = useState<string>();

      const [walletName, setWalletName] = useState<string>();
      useEffect(() => {
            const fetchData = async () => {
                  const name = await axios.get<string>(`/api/wallets/name`, {
                        params: {walletPublicKey}
                  });
                  setWalletName(name.data);
            }
            fetchData();
      }, [walletPublicKey]);

      const [walletBalance, setWalletBalance] = useState<string>();
      useEffect(() => {
            const fetchData = async () => {
                  const balance = await axios.get<string>(`/api/utxo/balance`, {
                              params: {walletPublicKey}
                        });
                  setWalletBalance(balance.data);
            }
            fetchData();
      }, [walletPublicKey]);

      const [totalBalance, setTotalBalance] = useState<string>();
      useEffect(() => {
            const fetchData = async () => {
                  const balance = await axios.get<string>(`/api/utxo/balance/total`);
                  setTotalBalance(balance.data);
            }
            fetchData();
      }, [walletPublicKey]);

      const [transactions, setTransactions] = useState<Map<string, string>>();
      useEffect(() => {
            const fetchData = async () => {
                  const transactions = await axios.get<Map<string, string>>(`/api/wallets/transactions`, {
                              params: {walletPublicKey}
                        });
                  setTransactions(transactions.data);
            }
            fetchData();
      }, [walletPublicKey]);
      
      const [createTransaction, setCreateTransaction] = useState<boolean>(false);

      return (
            <div className={styles.walletInfo}>
                  {modalPageData && 
                        <TransactionModalPage transaction={modalPageData} onClose={() => {setShowModalPage(undefined);}}/>}
                  
                  <div className={styles.walletName}>{walletName ?? "Undefined Wallet"}</div>
                  <div className={styles.block}>
                        <label htmlFor="publicKey">Address</label>
                        <div className={styles.publicKeyBlock}>
                              <div className={styles.publicKey} onClick={copyPublicKey} >
                                    <input name="publicKey" 
                                          className={styles.publicKeyBox} 
                                          value={walletPublicKey} 
                                          ref={pkInputRef}
                                          readOnly>
                                    </input>
                                    <img src={copyIcon} className={styles.copyIcon}></img>
                              </div>
                              <div className={`${styles.copiedMessage} ${wasCopied ? styles.showMessage : ''}`}>
                                    <img src={infoIcon}></img>
                                    Copied
                              </div>
                        </div>
                        <div className={styles.balanceInfo}>
                              <div className={styles.balanceBox}>
                                    <span className={styles.balanceLabel}>Wallet balance</span>
                                    <div className={styles.balance}>
                                          <img className={styles.icon} src={coinIcon}></img>
                                          {walletBalance} coins
                                    </div>
                              </div>
                              <div className={styles.balanceBox}>
                                    <span className={styles.balanceLabel}>Total balance</span>
                                    <div className={styles.balance}>
                                          <img className={styles.icon} src={coinIcon}></img>
                                          {totalBalance} coins
                                    </div>
                              </div>
                              <Button text='Send' icon={coinIcon} isActive={Number(walletBalance) > 0 } className={styles.button} onClick={() => {Number(walletBalance) > 0 && setCreateTransaction(true)}}/>
                              {
                                    createTransaction && (
                                          <CreateTransactionModalPage walletPublicKey={walletPublicKey} onClose={() => {setCreateTransaction(false);}}/>
                                    )
                              }
                        </div>
                  </div>
                  <div className={`${styles.block} ${styles.walletTransactions}`}>
                        <span className={styles.blockTitle}>Transactions</span>
                        <table>
                              <thead>
                                    <tr>
                                          <th>Date</th>
                                          <th>Amount</th>
                                          <th>Status</th>
                                          <th>Transaction ID</th>
                                    </tr>
                              </thead>
                              <tbody>
                                    {transactions && Object.keys(transactions).length > 0 && Object.entries(transactions).map(([key, value]) => (
                                          <tr key={key}>
                                                <td>{new Date(value['timeStamp']).toLocaleString()}</td>
                                                <td style={{color: (value['senderPublicKey'] == walletPublicKey ? "#FF0000" : "#008000")}}>
                                                      {value['senderPublicKey'] == walletPublicKey ? '-' : "+"}{value['amount'] / 100_000_000}
                                                </td>
                                                <td>{typeof value['status'] === 'string' && value['status'].length > 0 ? value['status'][0] + (value['status'] as string).substring(1).toLowerCase() : "ERROR"}</td>
                                                <td>
                                                      <a href="#" onClick={() => setShowModalPage(value)}>
                                                            {value['transactionId']}
                                                      </a>
                                                </td>
                                          </tr>))
                                    }
                              </tbody>
                        </table>
                        {(!transactions || Object.keys(transactions).length == 0) && (
                              <div className={styles.noTransactionsMessage}>
                                    <span>No transactions found</span>
                                    <img src={noTransactionsIcon}></img>
                              </div>
                        )}
                  </div>
            </div>
      );
}