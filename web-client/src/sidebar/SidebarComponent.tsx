import { useEffect, useRef, useState } from 'react';
import axios from 'axios';
import styles from './SidebarComponent.module.css';

import selectedIcon from '../assets/icons/selected_icon.png';
import editIcon from '../assets/icons/edit_icon.png';
import addWalletIcon from '../assets/icons/add_new_wallet_icon.png';
import { SidebarComponentType } from './SidebarComponentType';
import { NewWalletModalPage } from '../modal/NewWalletModalPage';
import { Wallet } from '../types/Wallet';


interface SidebarComponentProps {
      type : SidebarComponentType;
      isSelected : boolean;
      icon : string;
      onSelected : (option : string) => void;
      onWalletNameUpdated? : (walletNameUpdated : string) => void;
}

export const SidebarComponent : React.FunctionComponent<SidebarComponentProps> = ({ type, isSelected, icon, onSelected, onWalletNameUpdated }) => {
      let wallets : Map<String, Wallet> = new Map<String, Wallet>(); 
      const componentContentRef = useRef<HTMLDivElement>(null);
      const [height, setHeight] = useState('0px');
      const [updateComponentListTrigger, setUpdateComponentListTrigger] = useState<boolean>(false);
      
      const [isActive, setIsActive] = useState(false);
      useEffect(() => {
            setTimeout(() => {
                  isActive && componentContentRef.current ? setHeight(`${componentContentRef.current.scrollHeight}px`) : setHeight('0px');
            }, 100);
      }, [isActive, updateComponentListTrigger]);

      const [selectedElement, setSelectedElement] = useState<string>();

      const [componentContent, setComponentContent] = useState<any>();
      useEffect(() => {
            const fetchData = async () => {
                  switch(type){
                        case SidebarComponentType.WALLETS:
                              /*await axios.get<Map<string, string>>('/api/wallets')
                              .then((response) => {
                                    setComponentContent(response.data);
                              }).catch((e) => {
                                    console.error("Failed to load wallets", e);
                              });*/
                              break;
                        case SidebarComponentType.NETWORK:
                              setComponentContent(['Dashboard'/*, 'Mempool'*/]);
                              break;
                        case SidebarComponentType.NODE:
                              setComponentContent([]);
                              break;
                        case SidebarComponentType.SETTINGS:
                              setComponentContent([]);
                              break;
                  }
            };
            fetchData();
      }, [updateComponentListTrigger]);

      const [walletName, setWalletName] = useState<string>('');
      const [editingKey, setEditingKey] = useState<string>();
      const onChangeHandler = (e : React.ChangeEvent<HTMLInputElement>) => {
            setWalletName(e.target.value);
      };

      const updateWalletName = async (walletPublicKey : string, newName : string) => {
            await axios.patch('/api/wallets', {
                  walletPublicKey: walletPublicKey,
                  walletName: newName
            })
            .then(response => {
                  if (response.status == 200) {
                        componentContent[editingKey || ''] = walletName;
                        // update WalletInfo block
                        if(onWalletNameUpdated){
                              onWalletNameUpdated(walletPublicKey);
                        }
                  }
            });
            setEditingKey(undefined);
      };

      const createNewWallet = async () => {
            await axios.post('/api/wallets')
            .then(response => {
                  if (response.status == 200) {
                        setUpdateComponentListTrigger(!updateComponentListTrigger);
                  }
            })
            .catch((err) => {
                  console.error(err);
            });
      };

      const inputRef = useRef<HTMLInputElement>(null);
      useEffect(() => {
            if (inputRef.current) {
                  inputRef.current.focus();
            }
      }, [inputRef]);

      const [isNewWalletModalOpen, setIsNewWalletModalOpen] = useState<boolean>(false);

      return (
            <div className={`${styles.sidebarComponent} ${type == SidebarComponentType.NODE || type == SidebarComponentType.SETTINGS ? styles.inactiveSidebarComponent : ""}`}>
                  {
                        isNewWalletModalOpen && (
                              <NewWalletModalPage
                                    importWallet={(wallet: Wallet) => {
                                          console.log("New wallet imported:", wallet);
                                          wallets.set(wallet.publicKey, wallet);
                                          setComponentContent((prevContent: any) => ({
                                                ...prevContent,
                                                [wallet.publicKey]: wallet.name
                                          }));
                                          setUpdateComponentListTrigger(!updateComponentListTrigger);
                                    }}
                                    onClose={() => {
                                          setIsNewWalletModalOpen(false);
                                    }}
                              />
                        )
                  }
                  <div className={`${styles.sidebarComponentTitle} ${/*isSelected && componentContent.length == 0 ? styles.selectedComponent : */''}`} 
                        onClick={() => {
                              // if(componentContent && componentContent.length > 0){
                                    setIsActive(!isActive);
                              // }
                              // else {
                              //       onSelected("-1"); setSelectedElement("-1");
                              // }
                        }}>
                        <img className={styles.icon} src={icon}></img>
                        <span>{type}</span>
                        {type == SidebarComponentType.WALLETS && (
                              <img 
                                    className={styles.nameEditButton} 
                                    src={addWalletIcon} 
                                    onClick={(e) => {
                                          e.stopPropagation();
                                          //createNewWallet();
                                          setIsNewWalletModalOpen(true);
                                    }}
                              >
                              </img>)
                        } 
                  </div>
                  <div className={`${styles.sidebarElements}`} ref={componentContentRef} style={{maxHeight: height}}>
                        {componentContent && Object.entries(componentContent).map(([key, name]) => (
                              <div className={`${styles.sidebarElement} ${(isSelected && selectedElement == key) ? styles.selectedElement : ''}`} key={key} onClick={() => {onSelected(key); setSelectedElement(key);}}>
                                    {isSelected && selectedElement == key && <img className={styles.icon} src={selectedIcon}></img>}
                                    <input 
                                          ref={editingKey == key ? inputRef : undefined}
                                          className={styles.sidebarElementOption} 
                                          value={editingKey == key ? walletName : String(name)} 
                                          readOnly={editingKey == key ? false : true}
                                          onChange={(e) => onChangeHandler(e)}
                                          onBlur={() => {
                                                // input lost focus
                                                if(editingKey == key && walletName != String(name)){
                                                      updateWalletName(editingKey, walletName);
                                                }
                                          }}
                                          onKeyDown={(e: React.KeyboardEvent<HTMLInputElement>) => {
                                                if (e.key === "Enter" && editingKey == key) {
                                                      updateWalletName(editingKey, walletName);
                                                }
                                          }}
                                          autoFocus={editingKey == key}>
                                    </input>
                                    {type == SidebarComponentType.WALLETS && (
                                          <img 
                                                className={styles.nameEditButton} 
                                                src={editIcon} 
                                                onClick={(e) => {
                                                      e.stopPropagation(); 
                                                      setEditingKey(key);
                                                      setWalletName(String(name));
                                                      setTimeout(() => inputRef.current?.focus(), 0);
                                                }}
                                          >
                                          </img>)
                                    }    
                              </div>
                        ))}
                  </div>
            </div>   
      );
}