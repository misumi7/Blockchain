import styles from './Sidebar.module.css'
import { SidebarComponent } from './SidebarComponent'
import { SidebarComponentType } from './SidebarComponentType';

import walletIcon from '../assets/icons/wallet_icon.png';
import nodeIcon from '../assets/icons/node_icon.png';
import networkIcon from '../assets/icons/network_icon.png';
import settingsIcon from '../assets/icons/settings_icon.png';
import { useState } from 'react';

interface SidebarComponentProps {
      onComponentSelected : (componentType: SidebarComponentType, option : string) => void;
}

export const Sidebar : React.FC<SidebarComponentProps> = ({ onComponentSelected }) => {
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
                              icon={nodeIcon} 
                              onSelected={(option) => {
                                    setCurrentComponentSelected(SidebarComponentType.NODE);
                                    onComponentSelected(SidebarComponentType.NODE, option);
                              }}
                        />         
                        <SidebarComponent 
                              type={SidebarComponentType.SETTINGS} 
                              isSelected={currentComponentSelected == SidebarComponentType.SETTINGS} 
                              icon={settingsIcon} 
                              onSelected={(option) => {
                                    setCurrentComponentSelected(SidebarComponentType.SETTINGS);
                                    onComponentSelected(SidebarComponentType.SETTINGS, option);
                              }} 
                        />         
                  </div>       
            </div>
      );
} 