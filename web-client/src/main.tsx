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
    componentType: SidebarComponentType.WALLETS,
    option: 'MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgsy75RftrsHMhNHw0yJLyaT4n1ADoASqYoEQvzeYzrsSgBwYFK4EEAAqhRANCAASkXuBWf17lovz8RDXz4w3uge90GUS07/5e3Apqmx85xim2KwD551J/81zdfxzAg5G6JbsfDw3xGsyZKCwxJOTY'
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
