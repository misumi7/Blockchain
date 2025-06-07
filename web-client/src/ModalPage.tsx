import { useState } from 'react';
import styles from './ModalPage.module.css'

import closeIcon from './assets/icons/close_icon.png'

interface ModalPageProps {
      title : string;
      onClose : () => void;
}

export const ModalPage: React.FC<ModalPageProps> = ({ title, onClose}) => {
      return (
            <div className={styles.modalPage}>
                  <div className={styles.background} onClick={() => {onClose();}}></div>
                  <div className={styles.contentBox}>
                        <div className={styles.content}>
                              <img src={closeIcon} className={styles.iconButton} onClick={() => {onClose();}}></img>
                              <span className={styles.title}>{title}</span>
                        </div>
                  </div>
            </div>
      );
}