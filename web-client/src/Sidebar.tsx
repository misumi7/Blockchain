import styles from './Sidebar.module.css'
import { SidebarComponent } from './SidebarComponent'

export const Sidebar = () => {
      let walletList = 5; // 

      return (
            <div className={styles.sidebar}>
                  <div className={styles.sidebarComponents}>
                        <SidebarComponent/>         
                        <SidebarComponent/>         
                        <SidebarComponent/>         
                  </div>       
            </div>
      );
} 