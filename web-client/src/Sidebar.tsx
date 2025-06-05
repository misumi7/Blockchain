import styles from './Sidebar.module.css'
import { SidebarComponent } from './SidebarComponent'
import { SidebarComponentType } from './SidebarComponentType';

import walletIcon from './assets/icons/wallet_icon.png';
import nodeIcon from './assets/icons/node_icon.png';
import networkIcon from './assets/icons/network_icon.png';
import settingsIcon from './assets/icons/settings_icon.png';

export const Sidebar = () => {
      return (
            <div className={styles.sidebar}>
                  <div className={styles.sidebarComponents}>
                        <SidebarComponent type={SidebarComponentType.WALLETS} icon={walletIcon}/>         
                        <SidebarComponent type={SidebarComponentType.NETWORK} icon={networkIcon}/>         
                        <SidebarComponent type={SidebarComponentType.NODE} icon={nodeIcon}/>         
                        <SidebarComponent type={SidebarComponentType.SETTINGS} icon={settingsIcon}/>         
                  </div>       
            </div>
      );
} 