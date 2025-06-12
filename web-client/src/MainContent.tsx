import { WalletInfo } from './WalletInfo'
import styles from './MainContent.module.css'
import { SidebarComponentType } from './sidebar/SidebarComponentType';
import { Network } from './Network';
import { Mempool } from './Mempool';
import { useState } from 'react';

interface MainContentProps{
      contentType : string;
      option : string;
      walletNameUpdated : string;
      onWalletNameUpdateHandled : () => void;
}

export const MainContent: React.FC<MainContentProps> = ({ contentType, option, walletNameUpdated, onWalletNameUpdateHandled}) => {

      return (
            <div className={styles.mainContent}>
                  <div className={styles.panel}>
                        {
                              contentType == SidebarComponentType.WALLETS && (
                                    <WalletInfo 
                                          walletPublicKey={option} 
                                          walletNameUpdated={walletNameUpdated}
                                          onWalletNameUpdateHandled={onWalletNameUpdateHandled}
                                    />
                              )
                        }
                        {
                              contentType == SidebarComponentType.NETWORK && (() => {
                                    switch(option){
                                          case '0': 
                                                return <Network/>;
                                          case '1':
                                                return <Mempool/>
                                    }
                              })()
                        }
                  </div>
            </div>
      );
}