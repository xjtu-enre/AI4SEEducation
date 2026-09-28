---
sidebar_position: 1
---

## ENRE

> **IMPORTANT LICENSING UPDATE**
> At April 23, 2023, ENRE updated its license model to a *duel license model*:
>   * Any open-source usages that use or distribute open source applications under the LGPL can use the LGPL-licensed ENRE;
>   * Commercial redistributions should contact us to obtain a commertial license first.

> **重要许可更新**
> 2023年4月23日，ENRE更新了其许可模式为双重许可模式：
>   * 任何在LGPL下使用或分发开源应用程序的开源用途可以使用LGPL许可的ENRE；
>   * 商业再分发应首先联系我们以获得商业许可。

### WHAT IS ENRE?

🙋‍♀️ 
**ENRE** , developed by the SE team in Xi'an Jiaotong University, supports the extraction of **entities** and their **dependencies** from systems written in multiple languages, enables the customization of dependencies of interest to the user, and makes implicit dependencies explicit. 

### 什么是ENRE？
**ENRE** , 由西安交通大学软件工程团队开发，支持从多种语言编写的系统中提取 **实体** 及其 **依赖** 关系，允许用户自定义感兴趣的依赖关系，并使隐式依赖显式化。

### Features
- 📡 Supports multiple programming languages
- 🔗 Robust and flexible
- 🧰 Support possible dependencies in weak typing languages

### 特性
- 📡 支持多种编程语言
- 🔗 健壮且灵活
- 🧰 支持弱类型语言中的可能依赖关系

### Supports
⌛️ By doing static analysis, **ENRE** supports the following entities and dependency types.

|  Language  | Maximum Version | #Entity Type |#Dependency Type | Tool link |
|:----------:|:---------------:|:------------:|:---------------:|:----:|
|    Java    |       14        |       11     |    18       | [🔗](https://github.com/xjtu-enre/ENRE-java)|
|    C++     |       17        |       13    |    14   |[🔗](https://github.com/xjtu-enre/ENRE-cpp)|
|    Python  |       3.x       |       9     |    10    |[🔗](https://github.com/xjtu-enre/ENRE-py)
|    JS/TS    |       2022/4.7       |       17    |    12   |[🔗](https://github.com/xjtu-enre/ENRE-ts)
|    Golang    |      /       |       8    |    10   |[🔗](https://github.com/xjtu-enre/ENRE-go-python)

### 支持
⌛️ 通过静态分析, **ENRE** 支持以下实体和依赖类型。

|  语言  | 最高版本 | 实体类型数量 | 依赖类型数量	 | 工具链接 |
|:----------:|:---------------:|:------------:|:---------------:|:----:|
|    Java    |       14        |       11     |    18       | [🔗](https://github.com/xjtu-enre/ENRE-java)|
|    C++     |       17        |       13    |    14   |[🔗](https://github.com/xjtu-enre/ENRE-cpp)|
|    Python  |       3.x       |       9     |    10    |[🔗](https://github.com/xjtu-enre/ENRE-py)
|    JS/TS    |       2022/4.7       |       17    |    12   |[🔗](https://github.com/xjtu-enre/ENRE-ts)
|

<details>
<summary>Cite us</summary>
<div>
- Wuxia Jin et al., "Evaluating the Impact of Possible Dependencies on Architecture-level Maintainability," in IEEE Transactions on Software Engineering, doi: 10.1109/TSE.2022.3171288. 

    ```
    @ARTICLE{
        9765666,  
        author={Jin, Wuxia and Zhong, Dinghong and Cai, Yuanfang and Kazman, Rick and Liu, Ting},  
        journal={IEEE Transactions on Software Engineering},   
        title={Evaluating the Impact of Possible Dependencies on Architecture-level Maintainability},   
        year={2022},  
        volume={},  
        number={},  
        pages={1-1},  
        doi={10.1109/TSE.2022.3171288}
    }
    ```



- Wuxia Jin, et al., "Where to Start: Studying Type Annotation Practices in Python," 2021 36th IEEE/ACM International Conference on Automated Software Engineering (ASE), 2021, pp. 529-541, doi: 10.1109/ASE51524.2021.9678947.

    ```
    @INPROCEEDINGS{
        9678947,  
        author={Jin, Wuxia and Zhong, Dinghong and Ding, Zifan and Fan, Ming and Liu, Ting},  
        booktitle={2021 36th IEEE/ACM International Conference on Automated Software Engineering (ASE)},   
        title={Where to Start: Studying Type Annotation Practices in Python},   
        year={2021},  
        volume={},  
        number={},  
        pages={529-541},  
        doi={10.1109/ASE51524.2021.9678947}
    }
    ```

- Wuxia Jin et al. “Exploring the Architectural Impact of Possible Dependencies in Python Software.” 2020 35th IEEE/ACM International Conference on Automated Software Engineering (ASE) (2020): 758-770.

    ```
    @INPROCEEDINGS{
        9286085,  
        author={Jin, Wuxia and Cai, Yuanfang and Kazman, Rick and Zhang, Gang and Zheng, Qinghua and Liu, Ting},  
        booktitle={2020 35th IEEE/ACM International Conference on Automated Software Engineering (ASE)},   
        title={Exploring the Architectural Impact of Possible Dependencies in Python Software},   
        year={2020},  
        volume={},  
        number={},  
        pages={758-770},  
        doi={}
    }
    ```

- Wuxia Jin et al., "ENRE: A Tool Framework for Extensible eNtity Relation Extraction," 2019 IEEE/ACM 41st International Conference on Software Engineering: Companion Proceedings (ICSE-Companion), 2019, pp. 67-70, doi: 10.1109/ICSE-Companion.2019.00040.

    ```
    @INPROCEEDINGS{
        8802634,  
        author={Jin, Wuxia and Cai, Yuanfang and Kazman, Rick and Zheng, Qinghua and Cui, Di and Liu, Ting},  
        booktitle={2019 IEEE/ACM 41st International Conference on Software Engineering: Companion Proceedings (ICSE-Companion)},   
        title={ENRE: A Tool Framework for Extensible eNtity Relation Extraction},   
        year={2019},  
        volume={},  
        number={},  
        pages={67-70},  
        doi={10.1109/ICSE-Companion.2019.00040}
    }
    ```


- Wuxia Jin et al., Service Candidate Identification from Monolithic Systems based on Execution Traces. TSE 2019.


    ```
    @ARTICLE{
        8686152,  
        author={Jin, Wuxia and Liu, Ting and Cai, Yuanfang and Kazman, Rick and Mo, Ran and Zheng, Qinghua},
        journal={IEEE Transactions on Software Engineering},   
        title={Service Candidate Identification from Monolithic Systems Based on Execution Traces},
        year={2021},  
        volume={47},  
        number={5},  
        pages={987-1007},  
        doi={10.1109/TSE.2019.2910531}
    }
    ```
</div>
</details>