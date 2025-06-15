import { createRoot } from 'react-dom/client'
import { StrictMode, useState } from 'react'
import { Sidebar } from './sidebar/Sidebar'
import { MainContent } from './MainContent'

import './main.css';
import { SidebarComponentType } from './sidebar/SidebarComponentType';

function App() {
  const [selectedComponent, setSelectedComponent] = useState<{
    componentType: SidebarComponentType,
    option: string
  }>({
    componentType: SidebarComponentType.NODE,
    option: '0'
  });

  const [walletNameUpdated, setWalletNameUpdated] = useState<string>();

  return (
    <div className='mainScreen'>
      <Sidebar onComponentSelected={(componentType, option) => setSelectedComponent({ componentType, option })} onWalletNameUpdated={(walletNameUpdated) => setWalletNameUpdated(walletNameUpdated)}/>
      <MainContent 
        contentType={selectedComponent?.componentType ?? ''} 
        option={selectedComponent?.option ?? ''}
        walletNameUpdated={walletNameUpdated ?? ''}
        onWalletNameUpdateHandled={() => setWalletNameUpdated('')}  
      />
    </div>
  )
}


createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>
)
