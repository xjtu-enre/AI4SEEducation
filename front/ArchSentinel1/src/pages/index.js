import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import CoreFunctions from '@site/src/components/CoreFunctions';
import Industry from '@site/src/components/Industry';
import AcademicResearch from '@site/src/components/AcademicResearch';
import EcologicalPartner from '@site/src/components/EcologicalPartner';
import Bottom from '@site/src/components/Bottom';

import Heading from '@theme/Heading';
import styles from './index.module.css';

function HomepageHeader() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <Heading as="h1" className="hero__title">
          {siteConfig.title}
        </Heading>
        <p className="hero__subtitle">{siteConfig.tagline}</p>
        <div className={styles.buttons}>
          <a
            className={clsx('button button--primary button--lg', styles.systemButton)}
            href="http://localhost:8080/">
            进入 ArchSentinel 系统
          </a>
          <Link
            className="button button--secondary button--lg"
            to="/docs/docs/intro">
            查看功能说明
          </Link>
        </div>
      </div>
    </header>
  );
}

export default function Home() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout
      title="ArchSentinel Tech"
      description="面向代码类课程作业的 AI 评估与审查系统">
      <HomepageHeader />
      <main>
        {/* <HomepageFeatures /> */}
        <CoreFunctions />
        <Industry />
        <AcademicResearch />
        <EcologicalPartner />
        <Bottom />
      </main>
    </Layout >
  );
}
