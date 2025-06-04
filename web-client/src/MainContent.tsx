import { WalletInfo } from './WalletInfo'
import styles from './MainContent.module.css'

export const MainContent = () => {
      return (
            <div className={styles.mainContent}>
                  <div className={styles.panel}>
                        <WalletInfo/>
                  </div>
            </div>
      );
}