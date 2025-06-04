import { useEffect, useRef, useState } from 'react';
import styles from './SidebarComponent.module.css'

import walletIcon from './assets/icons/wallet_icon.png';
import selectedIcon from './assets/icons/selected_icon.png';

export const SidebarComponent = () => {
      const [isActive, setIsActive] = useState(false);
      const componentContentRef = useRef<HTMLDivElement>(null);
      const [height, setHeight] = useState('0px');

      const handleClick = () => {
            setIsActive(!isActive);
      }

      useEffect(() => {
            isActive && componentContentRef.current ? setHeight(`${componentContentRef.current.scrollHeight}px`) : setHeight('0px');
      }, [isActive]);

      return (
            <div className={styles.sidebarComponent}>
                  <div className={styles.sidebarComponentTitle} onClick={handleClick}>
                        <img className={styles.icon} src={walletIcon}></img>
                        <span>Wallets</span>
                  </div>
                  <div className={styles.sidebarElements} ref={componentContentRef} style={{maxHeight: height}}>
                        <div className={styles.sidebarElement}>
                              <span>Wallet {1}</span>      
                        </div>                  
                        <div className={`${styles.sidebarElement} ${styles.selectedElement}`}>
                              <img className={styles.icon} src={selectedIcon}></img>
                              <span>Wallet {2}</span>
                        </div>                  
                        <div className={styles.sidebarElement}>
                              <span>Wallet {3}</span>      
                        </div>                  
                        <div className={styles.sidebarElement}>
                              <span>Wallet {4}</span>
                        </div>                  
                        <div className={styles.sidebarElement}>
                              <span>Wallet {5}</span>
                        </div>           
                  </div>
            </div>   
      );
}