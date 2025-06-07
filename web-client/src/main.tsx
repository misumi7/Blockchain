import { createRoot } from 'react-dom/client'
import { StrictMode, useState } from 'react'
import { Sidebar } from './sidebar/Sidebar'
import { MainContent } from './MainContent'

import './main.css';
import { SidebarComponentType } from './sidebar/SidebarComponentType';

function App() {
                                                                                                     /*.NETWORK by default*/
  const [selectedComponent, setSelectedComponent] = useState<{
    componentType: SidebarComponentType,
    option: string
  }>({
    componentType: SidebarComponentType.WALLETS,
    option: 'MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAErfbjmzUy2nrRNC1vrfWh0wlYfwvMieoP5v72Ebsn8Jl9pa4s9Q65iqgsSgRlhSUBwm2zWD+ScCxKXSOu1eupDQ=='
  })

  return (
    <div className='mainScreen'>
      <Sidebar onComponentSelected={(componentType, option) => setSelectedComponent({ componentType, option })} />
      <MainContent contentType={selectedComponent?.componentType ?? ''} option={selectedComponent?.option ?? ''}/>
    </div>
  )
}


createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>
)
