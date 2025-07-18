import styles from './Sidebar.module.css'
import { SidebarComponent } from './SidebarComponent'
import { SidebarComponentType } from './SidebarComponentType';

import walletIcon from '../assets/icons/wallet_icon.png';
import networkIcon from '../assets/icons/network_icon.png';
import lockIcon from '../assets/icons/lock_icon.png';
import { useState } from 'react';

interface SidebarComponentProps {
      onComponentSelected : (componentType: SidebarComponentType, option : string) => void;
      onWalletNameUpdated : (isWalletNameUpdated : string) => void;
}

export const Sidebar : React.FC<SidebarComponentProps> = ({ onComponentSelected, onWalletNameUpdated }) => {
      const [currentComponentSelected, setCurrentComponentSelected] = useState<SidebarComponentType>();

      return (
            <div className={styles.sidebar}>
                  <div className={styles.sidebarComponents}>
                        <SidebarComponent 
                              type={SidebarComponentType.WALLETS} 
                              isSelected={currentComponentSelected == SidebarComponentType.WALLETS} 
                              icon={walletIcon} 
                              onSelected={(option) => {
                                    setCurrentComponentSelected(SidebarComponentType.WALLETS);
                                    onComponentSelected(SidebarComponentType.WALLETS, option);
                              }} 
                              onWalletNameUpdated={(walletNameUpdated) => {
                                    onWalletNameUpdated(walletNameUpdated);
                              }}
                        />         
                        <SidebarComponent 
                              type={SidebarComponentType.NETWORK} 
                              isSelected={currentComponentSelected == SidebarComponentType.NETWORK} 
                              icon={networkIcon} 
                              onSelected={(option) => {
                                    setCurrentComponentSelected(SidebarComponentType.NETWORK);
                                    onComponentSelected(SidebarComponentType.NETWORK, option);
                              }} 
                        />         
                        <SidebarComponent 
                              type={SidebarComponentType.NODE} 
                              isSelected={currentComponentSelected == SidebarComponentType.NODE} 
                              icon={lockIcon} 
                              onSelected={(option) => {
                                    setCurrentComponentSelected(SidebarComponentType.NODE);
                                    onComponentSelected(SidebarComponentType.NODE, option);
                              }}
                        />         
                        <SidebarComponent 
                              type={SidebarComponentType.SETTINGS} 
                              isSelected={currentComponentSelected == SidebarComponentType.SETTINGS} 
                              icon={lockIcon} 
                              onSelected={(option) => {
                                    setCurrentComponentSelected(SidebarComponentType.SETTINGS);
                                    onComponentSelected(SidebarComponentType.SETTINGS, option);
                              }} 
                        />         
                  </div>       
            </div>
      );
} 