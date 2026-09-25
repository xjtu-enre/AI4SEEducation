import React, { useState } from 'react';
import { Carousel } from 'react-responsive-carousel';
import 'react-responsive-carousel/lib/styles/carousel.min.css';

import styles from './styles.module.css';

const coreFunctionsData = [
    {
        title: 'ArchMapper',
        subTitle: '代码测绘仪',
        imageUrl: '/img/HomePage/core1.png',
        description: (
            <>
                代码元素与依赖分析，强调系统级依赖地图构建能力
            </>
        ),
        link: '/docs/ArchMapper'
    },
    {
        title: 'ArchInspector',
        subTitle: '质量巡检仪',
        imageUrl: '/img/HomePage/core2.png',
        description: (
            <>
                代码规范与安全检测，突显自动化扫描覆盖广度
            </>
        ),
        link: '/docs/ArchInspector'
    },
    {
        title: 'ArchCompass',
        subTitle: '架构指南针',
        imageUrl: '/img/HomePage/core3.png',
        description: (
            <>
                架构坏味道诊断，隐喻深度洞察架构隐患
            </>
        ),
        link: '/docs/ArchMapper'
    },
    {
        title: 'ArchVitals',
        subTitle: '架构体征仪',
        imageUrl: '/img/HomePage/core4.png',
        description: (
            <>
                健康指数量化评估，强化系统生命体征概念
            </>
        ),
        link: '/docs/ArchMapper'
    },
    {
        title: 'ArchDiagnose',
        subTitle: '智能诊断台',
        imageUrl: '/img/HomePage/core5.png',
        description: (
            <>
                整合所有检测数据，生成系统健康报告
            </>
        ),
        link: '/docs/ArchMapper'
    },
    {
        title: 'ArchBrain',
        subTitle: '架构智核',
        imageUrl: '/img/HomePage/core5.png',
        description: (
            <>
                知识库与智能决策，突出知识中枢与AI引擎定位
            </>
        ),
        link: '/docs/ArchMapper'
    },
    {
        title: 'ArchNurse',
        subTitle: '架构护理师',
        imageUrl: '/img/HomePage/core5.png',
        description: (
            <>
                重构方案推荐与实施，强化治理效果可视化
            </>
        ),
    },
];

export default function HomepageFeatures() {
    const [currentIndex, setCurrentIndex] = useState(0);

    const handleButtonClick = (index) => {
        setCurrentIndex(index);
    };

    return (
        <div className={styles.main}>
            <div className={styles.title}>核心功能介绍</div>

            <div className={styles.carouselContainer}>

                <ul className={styles.indicatorContainer}>
                    {coreFunctionsData.map((item, index) => (
                        <li key={index} className={styles.indicatorItem}>
                            <button
                                key={index}
                                className={
                                    currentIndex === index
                                        ? `${styles.indicatorButton} ${styles.indicatorButtonActive}`
                                        : styles.indicatorButton
                                }
                                onClick={() => handleButtonClick(index)}
                            >
                                <div className={styles.titleContainer}>
                                    <span>{item.title}</span>
                                    <span>{item.subTitle}</span>
                                </div>
                            </button>
                        </li>
                    ))}
                </ul>

                <Carousel
                    showArrows={true}
                    showStatus={false}
                    showIndicators={false}
                    infiniteLoop={true}
                    autoPlay={false}
                    interval={3000}
                    selectedItem={currentIndex}
                    onChange={(index) => setCurrentIndex(index)}
                >
                    {coreFunctionsData.map((item, index) => (
                        <div key={index} className={styles.carouselItem}>
                            <div className={styles.left}>
                                <h3 className={styles.itemTitle}>{item.title}</h3>
                                <h4 className={styles.itemSubTitle}>{item.subTitle}</h4>
                                <p className={styles.itemDesc}>{item.description}</p>
                            </div>
                            <div className={styles.right}>
                                <img src={item.imageUrl} alt={item.title} />
                            </div>
                        </div>
                    ))}
                </Carousel>
            </div>
        </div>
    );
}
