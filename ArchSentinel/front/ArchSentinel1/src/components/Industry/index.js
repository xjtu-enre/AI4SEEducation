import React from 'react';
import styles from './styles.module.css';


export default function HomepageFeatures() {

  return (
    <div className={styles.main}>
      <div className={styles.title}>赋能千行百业</div>
      <div className={styles.industryDiv}>
        <div className={styles.line1}>
          <div className={styles.item1}>
            <div className={styles.content}>
              <div className={styles.iconDiv}>
                <img src="img/HomePage/操作系统.png" alt="" />
              </div>
              <div className={styles.itemTitle}>操作系统</div>
              <div className={styles.itemTip}>Operating System</div>
            </div>
          </div>
          <div className={styles.item2}>
            <div className={styles.content}>
              <div className={styles.iconDiv}>
                <img src="img/HomePage/安全产品.png" alt="" />
              </div>
              <div className={styles.itemTitle}>安全产品</div>
              <div className={styles.itemTip}>Safety Product</div>
            </div>
          </div>
          <div className={styles.item3}>
            <div className={styles.content}>
              <div className={styles.iconDiv}>
                <img src="img/HomePage/人工智能.png" alt="" />
              </div>
              <div className={styles.itemTitle}>人工智能</div>
              <div className={styles.itemTip}>Artificial Intelligence</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
