import { useEffect, useRef, useState } from 'react';
import styles from './PinInput.module.css'

interface PinInputProps {
      setPinValues: (values: string[]) => void;
}

export const PinInput : React.FC<PinInputProps> = ({ setPinValues }) => {

      const [values, setValues] = useState(['', '', '', '', '', '']);
      const inputRefs = useRef<Array<HTMLInputElement | null>>([]);

      const onChangeHandler = (index : number, value : string) => {
            if(!new RegExp("^[0-9]?$").test(value)) {
                  return;
            }
            const newValues = [...values];
            newValues[index] = value;
            setValues(newValues);

            if(value && index < inputRefs.current.length - 1){
                  inputRefs.current[index + 1]?.focus();
            }
            else if(!value && index > 0){
                  inputRefs.current[index - 1]?.focus();
            }
      };

      useEffect(() => {
            setPinValues(values);
      }, [values]);

      return (
            <div className={styles.pinInput}>
                  <label>PIN Code</label>
                  <div className={styles.pinBox}>
                        {values.map((value, i) => (
                        <input
                              key={i}
                              type="text"
                              maxLength={1}
                              value={value}
                              onChange={(e) => onChangeHandler(i, e.target.value)}
                              ref={(el) => {inputRefs.current[i] = el}}
                        />
                        ))}
                  </div>
            </div>
      );
}