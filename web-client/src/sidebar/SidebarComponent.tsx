import { useEffect, useRef, useState } from 'react';
import axios from 'axios';
import styles from './SidebarComponent.module.css'

import selectedIcon from '../assets/icons/selected_icon.png';
import editIcon from '../assets/icons/edit_icon.png';
import { SidebarComponentType } from './SidebarComponentType';

interface SidebarComponentProps {
      type : SidebarComponentType;
      icon : string;
      onSelected : (option : string) => void;
}

export const SidebarComponent : React.FunctionComponent<SidebarComponentProps> = ({ type, icon, onSelected }) => {
      const [isActive, setIsActive] = useState(false);
      const componentContentRef = useRef<HTMLDivElement>(null);
      const [height, setHeight] = useState('0px');

      
      const handleClick = () => {
            setIsActive(!isActive);
      }
      
      useEffect(() => {
            isActive && componentContentRef.current ? setHeight(`${componentContentRef.current.scrollHeight}px`) : setHeight('0px');
      }, [isActive]);
      
      const [componentContent, setComponentContent] = useState<any>();
      useEffect(() => {
            const fetchData = async () => {
                  switch(type){
                        case SidebarComponentType.WALLETS:
                              try {
                                    const walletsPublicKeys = await axios.get<Map<string, string>>('/api/wallets');
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
            };
            fetchData();
      }, [setComponentContent]);

      const [editableOptions, setEditableOptions] = useState<Record<string, boolean>>({});
      const handleEditClick = (key: string) => {
            setEditableOptions(prev => ({ // ...prev are the values in prev, the 'key' will be overriden
                  ...prev,
                  [key]: true
            }));
      };

      return (
            <div className={styles.sidebarComponent}>
                  <div className={styles.sidebarComponentTitle} onClick={() => {handleClick();}}>
                        <img className={styles.icon} src={icon}></img>
                        <span>{type}</span>
                  </div>
                  <div className={styles.sidebarElements} ref={componentContentRef} style={{maxHeight: height}}>
                        {componentContent && Object.entries(componentContent).map(([key, name]) => (
                              <div className={styles.sidebarElement} key={key} onClick={() => {onSelected(key);}}>
                                    <input className={styles.sidebarElementOption} value={String(name)} readOnly={!editableOptions[key]}></input>
                                    {type == SidebarComponentType.WALLETS && (<img className={styles.nameEditButton} src={editIcon} onClick={(e) => {e.stopPropagation(); handleEditClick(key);}}></img>)}    
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