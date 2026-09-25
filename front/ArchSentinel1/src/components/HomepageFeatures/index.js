import React from 'react';

import clsx from 'clsx';
import Link from '@docusaurus/Link';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'ArchMapper',
    Svg: require('@site/static/img/undraw_docusaurus_mountain.svg').default,
    description: (
      <>
        架构测绘仪
      </>
    ),
    link: '/docs/ArchMapper'
  },
  {
    title: 'ArchInspector',
    Svg: require('@site/static/img/undraw_docusaurus_tree.svg').default,
    description: (
      <>
        架构巡检仪
      </>
    ),
    link: '/docs/ArchInspector'
  },
  {
    title: 'ArchVitals',
    Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
    description: (
      <>
        架构体征仪
      </>
    ),
    link: '/docs/ArchMapper'
  },
  {
    title: 'ArchCompass',
    Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
    description: (
      <>
        架构指南针
      </>
    ),
    link: '/docs/ArchMapper'
  },
  {
    title: 'ArchBrain',
    Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
    description: (
      <>
        架构智核
      </>
    ),
    link: '/docs/ArchMapper'
  },
  {
    title: 'archNurse',
    Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
    description: (
      <>
        架构护理师
      </>
    ),
  },
];

function Feature({ Svg, title, description, link }) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
        <Link className="button button--primary" to={link}>
          查看详情
        </Link>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
