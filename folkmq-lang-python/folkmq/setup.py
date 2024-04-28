#! /usr/bin/env python
# -*- coding: utf-8 -*_
from setuptools import setup,find_packages

setup(
    name='folkmq',
    version='1.4.2',
    description='@noear/folkmq python project',
    author='noear',
    url='https://folkmq.noear.org/',
    packages=find_packages(exclude=['*folkmq-test*']),   # 包内不需要引用的文件夹
    install_requires=[                          # 依赖包
        'socket.d'
    ],
    classifiers=[
        'Development Status :: 5 - Production/Stable',
        'Intended Audience :: Developers',
        'License :: OSI Approved',
        'Operating System :: OS Independent',
        'Programming Language :: Python',
        'Programming Language :: Python :: 3.12',
        'Topic :: Software Development :: Libraries'
    ],
    zip_safe=True,
    python_requires='>=3.12', # 建议使用3.12及以上
)
