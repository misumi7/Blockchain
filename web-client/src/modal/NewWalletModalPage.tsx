import { ModalPage } from './ModalPage';
import importIcon from '../assets/icons/import_icon.png'
import styles from './NewWalletModalPage.module.css'

interface NewWalletModalPageProps {
      onClose : () => void;
}

export const NewWalletModalPage : React.FC<NewWalletModalPageProps> = ({onClose}) => {
      return (
            <ModalPage className={styles.modal} onClose={() => onClose()}>
                  <div className={styles.importBlock}>
                        <img className={styles.importIcon} src={importIcon}></img>
                        <span className={styles.importLabel}>Import an existing one</span>
                  </div>
                  <div className={styles.createNewWalletButtonBox}>
                        <div className={styles.createNewWalletButton}>Create New</div>
                  </div>
            </ModalPage>
      );
}