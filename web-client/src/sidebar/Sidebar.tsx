import styles from './Sidebar.module.css'
import { SidebarComponent } from './SidebarComponent'
import { SidebarComponentType } from './SidebarComponentType';

import walletIcon from '../assets/icons/wallet_icon.png';
import nodeIcon from '../assets/icons/node_icon.png';
import networkIcon from '../assets/icons/network_icon.png';
import settingsIcon from '../assets/icons/settings_icon.png';

interface SidebarComponentProps {
      onComponentSelected : (componentType: SidebarComponentType, option : string) => void;
}

export const Sidebar : React.FC<SidebarComponentProps> = ({ onComponentSelected }) => {
      return (
            <div className={styles.sidebar}>
                  <div className={styles.sidebarComponents}>
                        <SidebarComponent type={SidebarComponentType.WALLETS} icon={walletIcon} onSelected={(option) => onComponentSelected(SidebarComponentType.WALLETS, option)} />         
                        <SidebarComponent type={SidebarComponentType.NETWORK} icon={networkIcon} onSelected={(option) => onComponentSelected(SidebarComponentType.NETWORK, option)} />         
                        <SidebarComponent type={SidebarComponentType.NODE} icon={nodeIcon} onSelected={(option) => onComponentSelected(SidebarComponentType.NODE, option)} />         
                        <SidebarComponent type={SidebarComponentType.SETTINGS} icon={settingsIcon} onSelected={(option) => onComponentSelected(SidebarComponentType.SETTINGS, option)} />         
                  </div>       
            </div>
      );
} 