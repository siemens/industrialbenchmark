import setuptools

with open("README.markdown", "r") as fh:
    long_description = fh.read()

setuptools.setup(
    name="industrial_benchmark_python",
    version="2.0",
    author="Daniel Hein, Stefan Depeweg, Michel Tokic, Steffen Udluft, Alexander Hentschel, Phillip Swazinna",
    author_email="hein.daniel@siemens.com",
    description="the industrial benchmark as a python package",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/siemens/industrialbenchmark.git",
    packages=setuptools.find_packages(),
    install_requires=["gym", "numpy"],
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    python_requires='>=3.6',
)