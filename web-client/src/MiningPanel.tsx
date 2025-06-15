import { useEffect, useRef, useState } from 'react';
import styles from './MiningPanel.module.css'
import showMoreIcon from './assets/icons/show_more_icon.png'
import axios from 'axios';
import { Button } from './Button';

interface MiningPanelProps {
      onWalletsUpdate : () => void;
}

export const MiningPanel : React.FC<MiningPanelProps> = ({ onWalletsUpdate }) => {
      const [wallets, setWallets] = useState<Map<string, string>>();
      const [isOpen, setIsOpen] = useState<boolean>(false);

      useEffect(() => {
            const fetchData = async () => {
                  await axios.get<Map<string, string>>(`/api/wallets`)
                  .then((response) => {
                        const walletsMap = new Map<string, string>(Object.entries(response.data));
                        setWallets(walletsMap);
                  });
            }
            fetchData();
      }, [onWalletsUpdate]);
      
      const [isMining, setIsMining] = useState<boolean>(false);
      const [walletSelected, setWalletSelected] = useState<string>();
      
      const [mempoolSize, setMempoolSize] = useState<number>(0);
      const [updateMempoolSize, setUpdateMempoolSize] = useState<boolean>(false);
      useEffect(() => {
            const fetchData = async () => {
                  await axios.get<number>('/api/nodes/mempool/count')
                  .then((response) => {
                        setMempoolSize(response.data)
                  }).catch((err) => {
                        console.error(err);
                  });
                  
            };
            fetchData();
      }, [updateMempoolSize]);

      const [miningReward, setMiningReward] = useState<number>(0);
      const [updateMiningReward, setUpdateMiningReward] = useState<boolean>(false);
      useEffect(() => {
            const fetchData = async () => {
                  await axios.get<number>('/api/blocks/mining-reward')
                  .then((response) => {
                        // console.log('Mining reward' + response.data);
                        setMiningReward(response.data / 100_000_000)
                  }).catch((err) => {
                        console.error(err);
                  });
                  
            };
            fetchData();
      }, [updateMiningReward]);

      const [logs, setLogs] = useState<string[]>([]);
      const [performance, setPerformance] = useState<number>(0);
      const [totalReward, setTotalReward] = useState<number>(0);
      useEffect(() => {
            let eventSource: EventSource;

            if (isMining) {
                  eventSource = new EventSource('/api/blocks/mining/logs');

                  eventSource.onmessage = (e) => {
                        setLogs(prev => [ ...prev, e.data ]);
                  };

                  eventSource.addEventListener("performance", (e) => {
                        setPerformance(e.data);
                  });

                  eventSource.addEventListener("mining-reward", (e) => {
                        console.log('Reward: ', e.data);
                        setTotalReward(totalReward + Number(e.data));
                  });

                  eventSource.onerror = (err) => {
                        console.error('Source error: ', err);
                        eventSource.close();
                  };

                  eventSource.onopen = () => {
                        const startMining = () => {
                              axios.get('/api/blocks/mine', { params: { minerPublicKey: walletSelected } })
                                    .then(response => {
                                          console.log('Mining started:', response.data);
                                    })
                                    .catch(err => {
                                          console.error('Mining error:', err);
                                          setTimeout(startMining, 5000);
                                    });
                        };
                        startMining();
                  };
            }
            else{
                  setTotalReward(0);
            }

            return () => {
                  if (eventSource) {
                        eventSource.close();
                  }
            };
      }, [isMining]);

      const scrollRef = useRef<HTMLDivElement>(null);
      useEffect(() => {
            if (scrollRef.current) {
                  scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
            }
      }, [logs, isMining]);

      // useEffect(() => {
      //       if(isMining && walletSelected){
      //             const mineBlockRequest = async () => {
      //                   await axios.get<string>('/api/blocks/mine', {
      //                         params: { minerPublicKey: walletSelected },
      //                   }).then((response) => {
      //                         console.log(response)
      //                   }).catch((err) => {
      //                         console.error(err);
      //                   });
                        
      //             };
      //             mineBlockRequest();
      //       }
      // }, [isMining]);


      return (
            <div className={styles.panel} onClick={() => setIsOpen(false)}>
                  <div className={styles.title}>Mining</div>
                  {/* <select className={styles.chooseWallet}>
                        {wallets && Object.keys(wallets).length > 0 && Object.entries(wallets).map(([key, value]) => (
                              <option key={key} value={key}>{value}</option>
                        ))}
                  </select> */}
                  {/* <span>Choose a wallet:</span> */}
                  <div className={styles.block}>
                        <div className={styles.section}>
                              <div className={styles.customSelect}>
                                    <div className={styles.firstOption} onClick={(e) => {e.stopPropagation(); setIsOpen(!isOpen);}} style={{color: walletSelected ? 'black' : ''}}>
                                          {
                                                wallets && walletSelected && wallets.get(walletSelected)
                                                      ? wallets.get(walletSelected)
                                                      : 'Choose a wallet for mining'
                                          }
                                          <img className={isOpen ? styles.openSelectionList : ''} src={showMoreIcon}></img>
                                    </div>
                                    {isOpen &&
                                          <div className={styles.options}>
                                                {wallets && wallets.size > 0 && Array.from(wallets.entries()).map(([key, value]) => (
                                                      <div className={styles.selectOption} key={key} onClick={() => { setWalletSelected(key); setIsOpen(false); }}>{value}</div>
                                                ))}
                                          </div>
                                    }
                              </div>
                              <div className={styles.miningInfo}>
                                    <span>Mempool: {`${mempoolSize} ${mempoolSize > 1 ? 'transactions' : 'transaction'}`}</span>
                              </div>
                        </div>
                        <Button text={isMining ? "Stop" : "Start"} isActive={true} className={`${styles.miningButton} ${isMining ? styles.stopMining : styles.startMining}`} onClick={() => {setIsMining(!isMining)}}/>
                  </div>
                  <div className={styles.logBlock} ref={scrollRef}>
                        {logs && logs.map((value, i) => (
                              <div key={i}>{value}</div>
                        ))}
                  </div>
                  <div className={styles.infoCards}>
                        <div className={styles.infoCard}>
                              <span>Performance</span>
                              <div>
                                    <span>{performance}</span>
                                    <span>H/s</span>
                              </div>
                        </div>
                        <div className={styles.infoCard}>
                              <span>Current reward</span>
                              <div>
                                    <span>{miningReward}</span>
                                    <span>coins/block</span>
                              </div>
                        </div>
                        <div className={styles.infoCard}>
                              <span>Session reward</span>
                              <div>
                                    <span>{(totalReward / 100_000_000).toFixed(3)}</span>
                                    <span>coins</span>
                              </div>
                        </div>

                  </div>
            </div>
      );
}