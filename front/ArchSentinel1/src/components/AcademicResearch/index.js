import React, { useState } from 'react';
import { Carousel } from 'react-responsive-carousel';
import 'react-responsive-carousel/lib/styles/carousel.min.css';
import styles from './styles.module.css'; // 引入你的样式

const coreFunctionsData = [
    {
        title: '论文',
        icon: '/img/HomePage/论文题目.png',
        list: [
            {
                name: 'Dependency Facade: The Coupling and Conflicts between Android Framework and Its Customization',
                author: 'Wuxia Jin, Yitong Dai, Jianguo Zheng, et al',
                stored: 'IEEE/ACM'
            },
            {
                name: 'The Design Smells Breaking the Boundary between Android Variants and AOSP',
                author: 'Wuxia Jin, Jiaowei Shang, Jianguo Zheng, Mengjie Zheng, Zhenyu Huang, Ming Fan, Ting Liu',
                stored: 'ICSE'
            },
            {
                name: 'ENRE: a tool framework for extensible eNtity relation extraction',
                author: 'Wuxia Jin, Yuanfang Cai, Rick Kazman, Qinghua Zheng, Di Cui, and Ting Liu',
                stored: 'ICSE'
            },
            {
                name: 'ERD-CQC : Enhanced Rule and Dependency Code Quality Check for Java',
                author: 'Yi Hou, Wuxia Jin, ZJ Liu, LM Wang, SG Chen, YH Wang, Lei Sang, HJ Wang, T Liu',
                stored: 'Internetware'
            },
            {
                name: 'ERD-CQC : Enhanced Rule and Dependency Code Quality Check for Java',
                author: 'Yi Hou, Wuxia Jin, ZJ Liu, LM Wang, SG Chen, YH Wang, Lei Sang, HJ Wang, T Liu',
                stored: 'Internetware'
            }
        ],
    },
    {
        title: '专利',
        icon: '/img/HomePage/专利.png',
        list: [
            { img: '/img/HomePage/patent.png' },
            { img: '/img/HomePage/patent.png' },
            { img: '/img/HomePage/patent.png' },
            { img: '/img/HomePage/patent.png' },
            { img: '/img/HomePage/patent.png' }
        ]
    },
    {
        title: '软著',
        icon: '/img/HomePage/软著.png',
        imageUrl: '/img/HomePage/image.png',
        list: [
            { img: '/img/HomePage/softness.png' },
            { img: '/img/HomePage/softness.png' },
            { img: '/img/HomePage/softness.png' },
            { img: '/img/HomePage/softness.png' }
        ]
    },
];

export default function HomepageFeatures() {
    const [currentCategoryIndex, setCurrentCategoryIndex] = useState(0);
    const [currentItemIndex, setCurrentItemIndex] = useState(0);

    const handleCategoryButtonClick = (index) => {
        setCurrentCategoryIndex(index);
        setCurrentItemIndex(0);
    };

    const handleCarouselChange = (index) => {
        setCurrentItemIndex(index);
    };

    const chunkList = (list) => {
        const chunked = [];
        for (let i = 0; i < list.length; i += 4) {
            chunked.push(list.slice(i, i + 4));
        }
        return chunked;
    };

    const currentList = coreFunctionsData[currentCategoryIndex].list;
    const chunkedList = chunkList(currentList);

    return (
        <div className={styles.main}>
            <div className={styles.title}>学术研究</div>

            <div className={styles.carouselContainer}>
                <ul className={styles.indicatorContainer}>
                    {coreFunctionsData.map((item, index) => (
                        <li key={index} className={styles.indicatorItem}>
                            <button
                                className={
                                    currentCategoryIndex === index
                                        ? `${styles.indicatorButton} ${styles.indicatorButtonActive}`
                                        : styles.indicatorButton
                                }
                                onClick={() => handleCategoryButtonClick(index)}
                            >
                                <div className={styles.titleContainer}>
                                    <img className={styles.titleContainerIcon} src={item.icon}></img>
                                    <span>{item.title}</span>
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
                    selectedItem={currentItemIndex}
                    onChange={handleCarouselChange}
                    centerMode={false}
                    dynamicHeight={false}
                    emulateTouch={true}
                    swipeable={true}
                    showThumbs={false}
                    stopOnHover={true}
                    // Customize the responsive settings to display 4 items at once
                    responsive={{
                        0: { items: 1 },
                        480: { items: 2 },
                        768: { items: 3 },
                        1024: { items: 4 }
                    }}
                >
                    {chunkedList.map((chunk, index) => (
                        <div key={index} style={{ padding: '20px', textAlign: 'center' }}>
                            <div className={styles.carousel}>
                                {chunk.map((item, idx) => (
                                    <div key={idx} className={styles.carouselItem}>
                                        {item.img ? (
                                            <div className={styles.carouselImg}>
                                                <img
                                                    src={item.img}
                                                    alt={`专利/软著 ${idx + 1}`}
                                                />
                                            </div>
                                        ) : (
                                            <div className={styles.carouselPaper}>
                                                <div className={styles.carouselPaperTitle}>{item.name}</div>
                                                <br />
                                                <div className={styles.carouselPaperAuthor}>{item.author}</div>
                                                <br />
                                                <div className={styles.carouselPaperStored}>{item.stored}</div>
                                            </div>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </Carousel>
            </div>
        </div>
    );
}
