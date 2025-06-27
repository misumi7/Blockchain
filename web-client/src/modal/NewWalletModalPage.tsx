import { ModalPage } from './ModalPage';
import importIcon from '../assets/icons/import_icon.png'
import styles from './NewWalletModalPage.module.css'
import { useRef, type ChangeEvent } from 'react';
import { Wallet } from '../types/Wallet';

import { handleWalletCreation } from '../utils/WalletUtils';

interface NewWalletModalPageProps {
      onClose : () => void;
      importWallet : (wallet : Wallet) => void;
}

export const NewWalletModalPage : React.FC<NewWalletModalPageProps> = ({onClose, importWallet}) => {
      const inputRef = useRef<HTMLInputElement>(null);

      const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
            const file = event.target.files?.[0];
            if (file) {
                  const reader = new FileReader();
                  reader.onload = (e) => {
                        try {
                              const walletJson = JSON.parse(e.target?.result as string);
                              if (walletJson.publicKey && 
                                    walletJson.encryptedPrivateKey && 
                                    walletJson.iv && 
                                    walletJson.salt) {
                                          const newWallet = new Wallet(
                                                walletJson.publicKey,
                                                walletJson.encryptedPrivateKey,
                                                walletJson.iv,
                                                walletJson.salt,
                                                file.name
                                          );
                                          importWallet(newWallet);
                              }
                        } 
                        catch (error) {
                              console.error("Error while reading the json", error);
                        }
                  };
                  reader.readAsText(file);     
            }
      };

      const handleClick = () => {
            inputRef.current?.click();
      };

      return (
            <ModalPage className={styles.modal} onClose={() => onClose()}>
                  <div className={styles.uploadWrapper}>
                        <input
                              type="file"
                              ref={inputRef}
                              onChange={handleFileChange}
                              style={{ display: "none" }}
                        />

                        <div className={styles.importBlock} onClick={handleClick}>
                              <img className={styles.importIcon} src={importIcon}/>
                              <span className={styles.importLabel}>Import an existing one</span>
                        </div>
                  </div>
                  <div className={styles.createNewWalletButtonBox}>
                        <div className={styles.createNewWalletButton} onClick={handleWalletCreation}>Create New</div>
                  </div>
            </ModalPage>
      );
}