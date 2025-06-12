import { useEffect, useRef, useState } from 'react';
import styles from './Network.module.css'

import BlockIcon from './assets/icons/block_icon.png'
import axios from 'axios';

interface NetworkProps {

}

function getTimePassed(timeStamp : number) : string {
      const sec = Math.floor((Date.now() - timeStamp) / 1000);
      if(sec < 60){
            return `${sec} seconds ago`;
      }
      const mins = Math.floor(sec / 60);
      if(mins < 60){
            return `${mins} minutes ago`;
      }
      const hours = Math.floor(mins / 60);
      if(hours < 60){
            return `${hours} hours ago`;
      }
      const days = Math.floor(hours / 24);
      if(days < 365){
            return `${days} days ago`;
      }
      const years = Math.floor(days / 365);
      return `${years} years ago`;
}

function getBlockchainSizeStr(size : number) : string {
      if(size < 11){
            return `${size} Bytes`;
      }
      const kilo = size / 1024;
      if(kilo < 1024){
            return `${kilo.toFixed(2)} KB`;
      }
      const mega = kilo / 1024;
      if(mega < 1024){
            return `${mega.toFixed(2)} MB`;
      }
      const giga = mega / 1024;
      return `${giga.toFixed(2)} GB`;
}

function getAddressComponents(addr : string) : string[] {
      const ip = addr.substring(addr.indexOf("://") + 3, addr.lastIndexOf(":"));
      const port = addr.substring(addr.lastIndexOf(":") + 1);
      return [ip, port];
}

export const Network : React.FC<NetworkProps> = ({ }) => {
      const AUTO_UPDATE = 1000 * 60 * 3;

      const [updateData, setUpdateData] = useState<boolean>(false);
      useEffect(() => {
            const timer = setTimeout(() => {
                  setUpdateData(!updateData);
                  console.log("Auto update");
            }, AUTO_UPDATE);
            return () => clearTimeout(timer);
      }, [updateData]);

      const scrollerRef = useRef<HTMLDivElement>(null);
      useEffect(() => {
            const el = scrollerRef.current;
            if (el) {
                  el.scrollLeft = el.scrollWidth;
                  const onWheel = (e: WheelEvent) => {
                        e.preventDefault();
                        const blockBox = el.querySelector(`.${styles.blockBox}`) as HTMLElement;
                        if (blockBox) {
                                const scrollAmount = blockBox.offsetWidth;
                                el.scrollTo({
                                          left: el.scrollLeft + (e.deltaY > 0 ? scrollAmount : -scrollAmount),
                                          behavior: "smooth"
                                });
                        }
                  };
                  el.addEventListener("wheel", onWheel);
                  return () => el.removeEventListener("wheel", onWheel);
            }
      }, []);

      const [size, setSize] = useState<number>(0);
      useEffect(() => {
            const fetchData = async () => {
                  await axios.get<Map<number, string>>(`/api/blocks/size`)
                  .then((response) => {
                        //console.log('Size:' + Number(response.data));
                        setSize(Number(response.data));
                  });
            }
            fetchData();
      }, [updateData]);

      const [mempoolSize, setMempoolSize] = useState<number>(0);
      useEffect(() => {
            const fetchData = async () => {
                  await axios.get<Map<number, string>>(`/api/nodes/mempool/size`)
                  .then((response) => {
                        setMempoolSize(Number(response.data));
                  });
            }
            fetchData();
      }, [updateData]);

      const [peersCount, setPeersCount] = useState<number>(0);
      useEffect(() => {
            const fetchData = async () => {
                  await axios.get<Map<number, string>>(`/api/nodes/peers/count`)
                  .then((response) => {
                        setPeersCount(Number(response.data));
                  });
            }
            fetchData();
      }, [updateData]);

      const [peers, setPeers] = useState<string[]>();
      useEffect(() => {
            const fetchData = async () => {
                  await axios.get<string[]>(`/api/nodes/peers`)
                  .then((response) => {
                        setPeers(response.data);
                  });
            }
            fetchData();
      }, [updateData]);

      const [fee, setFee] = useState<number>(0);
      useEffect(() => {
            const fetchData = async () => {
                  await axios.get<number>(`/api/transactions/fee`)
                  .then((response) => {
                        setFee(response.data);
                  });
            }
            fetchData();
      }, [updateData]);

      const [transactionCount, setTransactionCount] = useState<number>(0);
      useEffect(() => {
            const fetchData = async () => {
                  await axios.get<number>(`/api/blocks/transaction-count`)
                  .then((response) => {
                        setTransactionCount(response.data);
                  });
            }
            fetchData();
      }, [updateData]);

      const [blocks, setBlocks] = useState<Map<number, any>>(new Map());
      const [isLastAchieved, setIsLastAchieved] = useState(false);
      useEffect(() => {
            if (blocks && blocks.size > 0) {
                  console.log(Math.min(...Array.from(blocks.keys())));
            }
            const fetchData = async () => {
                  await axios.get<Map<number, string>>(`/api/blocks`, {
                        params: {
                              from: blocks && blocks.size > 0 ? Math.min(...Array.from(blocks.keys())) : -1,
                              count: 20,
                        }
                  }).then((response) => {
                        setBlocks(prev => {
                              const updatedBlocks = new Map(prev);
                              Object.entries(response.data).forEach(([id, data]) => {
                                    updatedBlocks.set(Number(id), data);
                              });
                              return updatedBlocks;
                        });
                  });
            }
            fetchData();
      }, [isLastAchieved]);

      const [lastBlockArrived, setLastBlockArrived] = useState<string>('');
      useEffect(() => {
            if (blocks && blocks.size > 0) {
                  const latestBlock = blocks.get(Math.max(...Array.from(blocks.keys())));
                  if (latestBlock && latestBlock.timeStamp != undefined) {
                        setLastBlockArrived(getTimePassed(latestBlock.timeStamp));
                  }
            }
      }, [blocks]);

      const [peerStatuses, setPeerStatuses] = useState<Record<string, boolean>>({});
      useEffect(() => {
            const checkPeers = async () => {
                  if (peers && peers.length > 0) {
                        const statuses: Record<string, boolean> = {};
                        await Promise.all(
                              peers.map(async (peer) => {
                                    await axios.get<any>(`/api/nodes/ping`, {
                                          params : {
                                                addr: peer
                                          }
                                    }).then((response) => {
                                          //console.log(`${peer}: ${response.data}`)
                                          statuses[peer] = response.status == 200;
                                    }).catch(() => {
                                          //console.log(`${peer}: ${err}`)
                                          statuses[peer] = false;
                                    });
                              })
                        );
                        setPeerStatuses(statuses);
                  }
            };
            checkPeers();
      }, [peers, updateData]);

      const [isBlockhainSynchronized, setIsBlockhainSynchronized] = useState<boolean>(false);
      useEffect(() => {
            const fetchData = async () => {
                  await axios.get<boolean>(`/api/blocks/sync`)
                  .then((response) => {
                        setIsBlockhainSynchronized(response.status == 200);
                  }).catch(() => {
                        setIsBlockhainSynchronized(false);
                  });
            }
            fetchData();
      }, [updateData]);

      return (
            <div className={styles.network}>
                  <div className={styles.networkInfo}>
                        <div className={`${styles.block} ${styles.blockhainInfo}`}>
                              <div className={styles.titleStatusBlock}>
                                    <span className={styles.blockTitle}>Blockchain</span>
                                    <div className={styles.blockchainStatus} /*title="Your blockhain is deprecated"*/>
                                          {isBlockhainSynchronized ? (
                                                <>
                                                      <span className={`${styles.greenDot}`}></span>
                                                      <span>Synchronized</span>
                                                </>
                                          ) :
                                          (
                                                <>
                                                      <span className={`${styles.redDot}`}></span>
                                                      <span>Out of Sync</span>
                                                </>
                                          )}
                                    </div>
                              </div>
                              <div className={styles.gridData}>
                                    <div className={styles.dataBlock}>
                                          <span className={styles.category}>Size</span>
                                          <div className={styles.data}>
                                          <span>{getBlockchainSizeStr(size).split(' ')[0]}</span> 
                                          <span>{getBlockchainSizeStr(size).split(' ')[1]}</span> 
                                          </div>
                                    </div>

                                    <div className={styles.dataBlock}>
                                          <span className={styles.category}>Mempool</span>
                                          <div className={styles.data}>
                                          <span>{getBlockchainSizeStr(mempoolSize).split(' ')[0]}</span> 
                                          <span>{getBlockchainSizeStr(mempoolSize).split(' ')[1]}</span> 
                                          </div>
                                    </div>
                                    <div className={styles.dataBlock}>
                                          <span className={styles.category}>Connections</span>
                                          <div className={styles.data}>
                                          <span>{peersCount}</span> 
                                          <span>Peers</span> 
                                          </div>
                                    </div>
                                    <div className={styles.dataBlock}>
                                          <span className={styles.category}>Last Block</span>
                                          <div className={styles.data}>
                                          <span>{lastBlockArrived.split(' ')[0]}</span> 
                                          <span>{lastBlockArrived.split(' ')[1]}</span> 
                                          </div>
                                    </div>
                                    <div className={styles.dataBlock}>
                                          <span className={styles.category}>Fee</span>
                                          <div className={styles.data}>
                                          <span>{fee}</span> 
                                          <span>m/byte</span> 
                                          </div>
                                    </div>
                                    <div className={styles.dataBlock}>
                                          <span className={styles.category}>Transactions</span>
                                          <div className={styles.data}>
                                          <span>{transactionCount}</span> 
                                          </div>
                                    </div>
                              </div>
                        </div>
                        <div className={`${styles.block} ${styles.peers}`}>
                              <span className={styles.blockTitle}>Peers</span>
                              <table className={styles.peerTable}>
                                    <thead>
                                          <tr>
                                                <th>IP</th>
                                                <th>Port</th>
                                                <th></th>
                                          </tr>
                                    </thead>
                                    <tbody>
                                          {peers && peers.length > 0 && peers.map((value, i) => {
                                                const [ip, port] = getAddressComponents(value);
                                                return (
                                                      <tr key={i}>
                                                            <td>{ip}</td>
                                                            <td>{port}</td>
                                                            <td>
                                                                  <div className={styles.status}>
                                                                        {peerStatuses[value] ? 
                                                                              (<>
                                                                                    <span className={styles.greenDot}></span>
                                                                                    <span>Active</span>
                                                                              </>)
                                                                              :
                                                                              (<>
                                                                                    <span className={styles.redDot}></span>
                                                                                    <span>Dead</span>
                                                                              </>)
                                                                        }
                                                                  </div>
                                                            </td>
                                                      </tr>
                                                );
                                          })}
                                    </tbody>
                              </table>
                        </div>
                  </div>
                  <div className={`${styles.block} ${styles.latestBlocks}`}>
                        <span className={styles.blockTitle}>Latest Blocks</span>
                        <div className={styles.blockLine}>
                              <div className={styles.blockPath}></div>
                              <div className={styles.blockScroller} ref={scrollerRef}>
                                    {blocks && blocks.size > 0 && Array.from(blocks.entries()).map(([id, data]) => (
                                          <div className={styles.blockBox} key={id}>
                                                <img className={styles.blockIcon} src={BlockIcon}></img>
                                                <div className={styles.blockData}>
                                                      <span>Block #{id}</span>
                                                      <span>{data.transactions.length} {data.transactions.length > 1 ? 'transactions' : 'transaction'}</span>
                                                      <span>{getTimePassed(data.timeStamp)}</span>
                                                </div>
                                          </div>
                                    ))}
                              </div>
                        </div>
                  </div>
            </div>
      );
};