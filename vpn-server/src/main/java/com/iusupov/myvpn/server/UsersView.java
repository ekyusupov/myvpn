package com.iusupov.myvpn.server;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldBase;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * @author Ernest Iusupov
 */
@Route("")
public class UsersView extends VerticalLayout {
    private final UserRepository users;
    private final Grid<User> userGrid;

    public UsersView(final UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        userGrid = createUserGrid();

        updateUserGrid();
        add(new H1("Hello"), createForm(passwordEncoder), userGrid);
    }

    private Grid<User> createUserGrid() {
        final Grid<User> userGrid;
        userGrid = new Grid<>(User.class);
        userGrid.removeAllColumns();
        userGrid.addColumn("username").setHeader("Username");
        userGrid.addColumn("name").setHeader("Name");
        userGrid.addColumn(new ComponentRenderer<>(
                Button::new,
                (button, user) -> { //настраиваем кнопку и юзера
                    button.setText("Remove");
                    button.addClickListener(event -> {
                        final ConfirmDialog dialog = new ConfirmDialog();
                        dialog.setHeader("Remove user");
                        dialog.setText("Remove user %s (id=%s)".formatted(user.getName(), user.getId()));
                        dialog.setCancelable(true);
                        dialog.addConfirmListener(confirm -> {
                            users.delete(user);
                            updateUserGrid();
                        });
                        dialog.open();
                    });
                }
        )).setHeader("Actions");
        return userGrid;
    }

    private void updateUserGrid() {
        userGrid.setItems(this.users.findAll());
    }

    private Component createForm(PasswordEncoder passwordEncoder) {
        final TextField username = new TextField("Username");
        final TextField name = new TextField("Name");
        final PasswordField password = new PasswordField("Password");

        final Binder<User> binder = new Binder<>(User.class);

        final TextField field = username;
        final String property = "username";
        bind(binder, field, property, 3);
        bind(binder, name, "name", 0);
        bind(binder, password,"password", 5);

        final Button add = new Button("Add user");
        add.addClickListener(event -> {
            final User user = new User();
            try {
                binder.writeBean(user); //с помощью binder записываем в него значения всех наших полей
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                users.save(user);
                binder.readBean(new User()); //записываем нового пустого пользователя
                updateUserGrid(); //обновляем
            } catch (ValidationException ignore) {
            }
        });
        add.addClickShortcut(Key.ENTER);
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY); //меняем вид кнопки

        final HorizontalLayout form = new HorizontalLayout(username, name, password, add);
        form.setAlignItems(Alignment.BASELINE);
        return form;
    }

    private void bind(Binder<User> binder,TextFieldBase<?, String> field, String property, int minLength) {
        binder.forField(field)
                .withValidator(
                        value -> value.length() >= minLength,
                        "Should contain at least %s characters".formatted(minLength)
                )
                .bind(property);
    }
}
