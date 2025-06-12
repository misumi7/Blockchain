import styles from './ModalPage.module.css'
import closeIcon from '../assets/icons/close_icon.png'

interface ModalPageProps {
      onClose : () => void;
      children : React.ReactNode;
      className?: string;
}

export const ModalPage: React.FC<ModalPageProps> = ({ children, onClose, className}) => {
      return (
            <div className={`${styles.modalPage}`}>
                  <div className={styles.background} onClick={() => {onClose();}}></div>
                  <div className={`${styles.contentBox} ${className}`}>
                        <div className={styles.content}>
                              <img src={closeIcon} className={styles.iconButton} onClick={() => {onClose();}}></img>
                              {children}
                        </div>
                  </div>
            </div>
      );
}