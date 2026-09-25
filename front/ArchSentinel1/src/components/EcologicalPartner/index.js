import React from 'react';
import styles from './styles.module.css'; // 引入你的样式


export default function HomepageFeatures() {

    return (
        <div className={styles.main}>
            <div className={styles.title}>合作伙伴</div>
            <div className={styles.ecology}>
                <div className={styles.line}>
                    <span>即将揭晓</span>
                    {/* <img className={styles.img1} src='img/HomePage/荣耀.png'></img>
                    <img className={styles.img2} src='img/HomePage/华为-copy.png'></img> */}
                </div>
            </div>

        </div>
    );
}
