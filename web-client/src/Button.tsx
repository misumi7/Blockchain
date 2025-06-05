import styles from './Button.module.css'

interface ButtonProps {
      text : string;
      icon? : string;
      iconRight? : boolean;  
      className? : string;
}

export const Button : React.FC<ButtonProps> = ({ text, icon, iconRight, className }) => {
      return (
            <div className={`${styles.button} ${className}`}>
                  {icon && !iconRight && 
                  (<img src={icon} className={styles.buttonIcon}></img>)}

                  <span>{text}</span>

                  {icon && iconRight && 
                  (<img src={icon} className={styles.buttonIcon}></img>)}
            </div>
      );
}