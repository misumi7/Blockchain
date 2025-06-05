import { useEffect, useRef, useState } from 'react';
import axios from 'axios';
import styles from './SidebarComponent.module.css'

import selectedIcon from './assets/icons/selected_icon.png';
import { SidebarComponentType } from './SidebarComponentType';

interface SidebarComponentProps {
      type : SidebarComponentType;
      icon : string;
}

export const SidebarComponent : React.FunctionComponent<SidebarComponentProps> = ({ type, icon }) => {
      const [isActive, setIsActive] = useState(false);
      const componentContentRef = useRef<HTMLDivElement>(null);
      const [height, setHeight] = useState('0px');

      const [componentContent, setComponentContent] = useState<string[]>([]);

      const handleClick = () => {
            setIsActive(!isActive);
      }

      useEffect(() => {
            isActive && componentContentRef.current ? setHeight(`${componentContentRef.current.scrollHeight}px`) : setHeight('0px');
      }, [isActive]);

      useEffect(() => {
            const fetchData = async () => {
                  switch(type){
                        case SidebarComponentType.WALLETS:
                              try {
                                    const walletsPublicKeys = await axios.get<string[]>('/api/wallets');
                                    setComponentContent(walletsPublicKeys.data);
                              }
                              catch(e){
                                    console.error("Failed to load wallets", e);
                              }
                              break;
                        case SidebarComponentType.NETWORK:
                              setComponentContent(['an']);
                              break;
                        case SidebarComponentType.NODE:
                              setComponentContent(['ann']);
                              break;
                        case SidebarComponentType.SETTINGS:
                              setComponentContent(['as']);
                              break;
                  }
                  return ['a'];
            };
            fetchData();
      }, [setComponentContent]);

      return (
            <div className={styles.sidebarComponent}>
                  <div className={styles.sidebarComponentTitle} onClick={handleClick}>
                        <img className={styles.icon} src={icon}></img>
                        <span>{type}</span>
                  </div>
                  <div className={styles.sidebarElements} ref={componentContentRef} style={{maxHeight: height}}>
                        {componentContent.map((item, i) => (
                              <div className={styles.sidebarElement} key={i}>
                                    <span>{item}</span>      
                              </div>
                        ))}                  
                        {/* <div className={`${styles.sidebarElement} ${styles.selectedElement}`}>
                              <img className={styles.icon} src={selectedIcon}></img>
                              <span>Wallet {2}</span>
                        </div>                   */}     
                  </div>
            </div>   
      );
}