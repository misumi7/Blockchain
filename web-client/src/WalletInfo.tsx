import { useState } from 'react';
import styles from './WalletInfo.module.css'
import infoIcon from './assets/icons/info_icon.png';

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
                  <div className={styles.walletTitle}>Wallet 2</div>
                  <label htmlFor="publicKey">Address</label>
                  <div className={styles.publicKeyBlock}>
                        <input name="publicKey" 
                              className={styles.publicKeyBox} 
                              value="5c82ce5b5fcdfbd54138d595fcfed1071705a99ea7985b1df38cc69ec320dace" 
                              onClick={copyPublicKey} 
                              readOnly>
                        </input>
                        <div className={`${styles.copiedMessage} ${wasCopied ? styles.showMessage : ''}`}>
                              <img className={styles.icon} src={infoIcon}></img>
                              Copied
                        </div>
                  </div>
            </div>
      );
}