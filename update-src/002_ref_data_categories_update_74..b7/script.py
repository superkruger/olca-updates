def main():
    olca.eachCategory(update_category_name)


def update_category_name(category):
    if category.modelType.name() == 'FLOW':
        categoryPath = get_category_path(category)
        if (categoryPath == "Elementary flows\\air\\" or categoryPath == "Elementary flows\\soil\\" or categoryPath == "Elementary flows\\water\\"):
            new_name = 'Emission to ' + category.name
            category.name = new_name
            olca.updateCategory(category)
        elif categoryPath == "Elementary flows\\resource\\":
            new_name = 'Resource'
            category.name = new_name
            olca.updateCategory(category)


def get_category_path(cat):
    path = "%s\\" % (cat.name)
    while cat.category is not None:
        cat = cat.category
        path = "%s\\%s" % (cat.name, path)
    return path

if __name__ == '__main__':
    main()
