import { useState } from 'react';
import styles from './WalletInfo.module.css'

import infoIcon from './assets/icons/info_icon.png';
import coinIcon from './assets/icons/coin_icon.png';
import { Button } from './Button';

export const WalletInfo = () => {
      const [wasCopied, setCopied] = useState(false);

      const copyPublicKey = (e: React.MouseEvent<HTMLInputElement>) => {
            e.currentTarget.select();
            navigator.clipboard.writeText(e.currentTarget.value).then(() => {
                  setCopied(true);
                  setTimeout(() => {
                        setCopied(false);
                  }, 1000);
            });
      }

      return (
            <div className={styles.walletInfo}>
                  <div className={styles.walletName}>Wallet 2</div>
                  <div className={styles.block}>
                        <label htmlFor="publicKey">Address</label>
                        <div className={styles.publicKeyBlock}>
                              <input name="publicKey" 
                                    className={styles.publicKeyBox} 
                                    value="5c82ce5b5fcdfbd54138d595fcfed1071705a99ea7985b1df38cc69ec320dace" 
                                    onClick={copyPublicKey} 
                                    readOnly>
                              </input>
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
                                          2.132 coins
                                    </div>
                              </div>
                              <div className={styles.balanceBox}>
                                    <span className={styles.balanceLabel}>Total balance</span>
                                    <div className={styles.balance}>
                                          <img className={styles.icon} src={coinIcon}></img>
                                          2.132 coins
                                    </div>
                              </div>
                              <Button text='Send' icon={coinIcon} className={styles.button}/>
                        </div>
                  </div>
                  <div className={`${styles.block} ${styles.walletTransactions}`}>
                        <span className={styles.blockTitle}>Transactions</span>
                        <table>
                              <tr>
                                    <th>Date</th>
                                    <th>Amount</th>
                                    <th>Status</th>
                                    <th>Transaction ID</th>
                              </tr>

                              <tr>
                                    <td>02.06.2025 14:20</td>
                                    <td>1.120</td>
                                    <td>Pending</td>
                                    <td><a href="#">fe6c9003ffe1f5120ccfe5db03b23b21d17964b2e05f36ce17ca5af82d75df11</a></td>
                              </tr>
                              <tr>
                                    <td>01.10.2023 18:01</td>
                                    <td>10.782</td>
                                    <td>Confirmed</td>
                                    <td><a href="#">5757153b028c4322c817552dacd608a4255ccce12836314699e0b3d4a273895d</a></td>
                              </tr>
                              <tr>
                                    <td>18.04.2013 08:23</td>
                                    <td>2.132</td>
                                    <td>Confirmed</td>
                                    <td><a href="#">72fd3d8f8cc2a3cb343bc9548b5a4c9c5200f3b0686cfb8e99bdf2a07d504031</a></td>
                              </tr>
                              <tr>
                                    <td>17.03.2011 18:57</td>
                                    <td>0.517</td>
                                    <td>Confirmed</td>
                                    <td><a href="#">f2f27cc408748cc55a38f515146bf771deaeda090aeabeec41a82877183c9b7d</a></td>
                              </tr>
                        </table>
                  </div>
            </div>
      );
}