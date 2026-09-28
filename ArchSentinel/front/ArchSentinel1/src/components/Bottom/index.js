import React from 'react';
import styles from './styles.module.css';


export default function HomepageFeatures() {

    return (
        <div className={styles.main}>
            <div className={styles.btBg}>
                <div className={styles.btLogo}></div>
                <div className={styles.btTitle}>ArchSentinel</div>
                <div className={styles.unit}>
                    <div className={styles.tip}>发起单位：</div>
                    <div className={styles.unitItem}></div>
                    <div className={styles.unitItemText}>西安交通大学电信学部软件学院</div>
                </div>
            </div>
        </div>
    );
}
