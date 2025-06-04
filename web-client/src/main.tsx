import { createRoot } from 'react-dom/client'
import { StrictMode } from 'react'
import { Sidebar } from './Sidebar'
import { MainContent } from './MainContent'

import './main.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <div className='mainScreen'>
      <Sidebar/>
      <MainContent/>
    </div>
  </StrictMode>
)
