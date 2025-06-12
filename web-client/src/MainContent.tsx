import { WalletInfo } from './WalletInfo'
import styles from './MainContent.module.css'
import { SidebarComponentType } from './sidebar/SidebarComponentType';
import { Network } from './Network';

interface MainContentProps{
      contentType : string;
      option : string;
}

export const MainContent: React.FC<MainContentProps> = ({ contentType, option}) => {
      return (
            <div className={styles.mainContent}>
                  <div className={styles.panel}>
                        {
                              contentType == SidebarComponentType.WALLETS && (
                                    <WalletInfo walletPublicKey={option}/>
                              )
                        }
                        {
                              contentType == SidebarComponentType.NETWORK && (
                                    <Network/>
                              )
                        }
                  </div>
            </div>
      );
}