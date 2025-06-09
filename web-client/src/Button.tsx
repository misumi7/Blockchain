import styles from './Button.module.css'

interface ButtonProps {
      text : string;
      icon? : string;
      iconRight? : boolean;  
      className? : string;
      isActive? : boolean;
      onClick? : () => void;
      hideText? : boolean;
}

export const Button : React.FC<ButtonProps> = ({ text, icon, iconRight, hideText, className, isActive, onClick }) => {
      return (
            <div className={`${styles.button} ${className} ${isActive ? '' : styles.inactiveButton}`} onClick={() => { onClick && onClick(); }}>
                  {icon && !iconRight && 
                  (<img src={icon} className={`${styles.buttonIcon} ${hideText ? styles.centerIcon : ''}`}></img>)}

                  <span className={hideText ? styles.hideText : ''}>{text}</span>

                  {icon && iconRight && 
                  (<img src={icon} className={`${styles.buttonIcon} ${hideText ? styles.centerIcon : ''}`}></img>)}
            </div>
      );
}